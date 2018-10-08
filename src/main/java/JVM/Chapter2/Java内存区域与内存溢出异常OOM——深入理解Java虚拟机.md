# Java内存区域与OOM内存溢出异常

http://blog.csdn.net/xuranzyy/article/details/71248727

###**一、Java运行时数据区域**
![JVM运行时数据区域](http://img.blog.csdn.net/20170506092640329?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveHVyYW56eXk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
Java虚拟机在执行Java程序的过程中把它管理的内存划分为若干个数据区域。这些区域有不同的用途、生命周期（创建与销毁）。根据Java虚拟机规范，JVM所管理的内存分为如下几个部分：

 - **程序计数器**
 - **Java虚拟机栈**
 - **本地方法栈**
 - **方法区**
 - **运行时常量池**

--------------------------------
1. **程序计数器**（**此内存区域无OOM异常**）
	程序计数器`PC Register`是一块较小的内存区域，可看作是当前线程所执行字节码的行号指示器。字节码解释器工作时通过改变`PC`的值选取**下一条即将执行的字节码指令**。
	由于**`Java`多线程**通过线程轮流切换、执行实现，所以为了线程切换之后能恢复到正确的执行位置，每条线程都有自己**独立的一个程序计数器`PC`**，这片区域为线程私有的内存区域。
		如果线程正在执行`Java`方法，`PC`记录的是**正在执行的虚拟机字节码指令地址**；如果正在执行本地`native`方法，则`PC`的值为**空（`undefined`）**。
		
2. **`Java`虚拟机栈**
		`Java`虚拟机栈是线程私有的，生命周期与线程相同。`Java`虚拟机栈描述的是`Java`方法执行的内存模型。方法在执行的同时（包括方法之间的调用） 会创建**栈帧(`Stack Frame`)**，用于存储局部变量表、操作数栈、动态链接、方法出口等。每个方法从调用到执行完成，就对应一个栈帧在虚拟机栈中的入栈、出栈操作。
		`OOM`异常:
		(1) 如果线程请求分配的栈容量超过`Java`虚拟机栈允许的最大容量，抛出**`StackOverflowError`**异常。
		(2) 如果Java虚拟机栈可以动态扩展，如果扩展时无法申请到足够的内存或者新建线程的时候没有足够的内存空间去创建`Java`虚拟机栈，抛出**`OutOfMemoryError`**异常。
		
3. **本地方法栈**
		与`Java`虚拟机栈类似，本地方法栈为虚拟机使用到的`native`方法服务。`OOM`异常与`Java`虚拟机栈类似。
		`OOM`异常:
		(1) 如果线程请求分配的栈容量超过`Java`本地方法栈允许的最大容量，抛出**`StackOverflowError`**异常。
		(2) 如果本地方法栈可以动态扩展，如果扩展时无法申请到足够的内存或者新建线程的时候没有足够的内存空间去创建本地方法栈，抛出**`OutOfMemoryError`**异常。

4.  **`Java`堆（`Java Heap`）**
		`Java Heap`是`JVM`所管理的内存中内存最大的一块。`Java Heap`是**被所有线程共享**的一块内存区域，在虚拟机启动时创建。所有的对象实例以及数组都在堆上分配内存。
		从内存回收的角度看，由于现代收集器采用分代收集算法，从垃圾回收的角度看，`Java Heap`可以分为**新生代、老年代**。
		`Java Heap`可以处在物理上不连续的内存空间中，只要逻辑上连续就可以。
		`OOM`异常(`-Xmx堆最大容量`  `-Xms`堆初始化容量)：
		(1) 如果在堆中没有内存完成实例分配，并且堆也无法扩展，抛出**`OutOfMemoryError`**异常。

5. **方法区（`Method Area`）**——`Non-Heap`(**非堆**)
		方法区是由各个线程共享的内存区域，用于存储**已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据**。
		方法区又称为**永久代(`Permanent Generation`)**，这是由于`Hotspot VM`选择把`GC`分代收集扩展至方法区，或者说使用永久代实现方法区。
		`OOM`异常(`-XX:MaxPermSize` 设置永久代内存最大值):
		(1) 当方法区无法满足内存分配需求时，抛出**`OutOfMemoryError`**异常。

6. **运行时常量池(`Runtime Constant Pool`)**
		是方法区的一部分，`Class`文件中除了有类的版本、字段、方法、接口等描述信息外，还有一项信息就是常量池，**用于存放编译期间生成的各种字面量和符号引用。**
		`OOM`异常:
		(1)当创建类或接口的时候，如果构造运行时常量池所需要的内存空间超过了方法区所能提供的最大值，抛出**`OutOfMemoryError`**异常。

###**二、`HotSpot`虚拟机对象创建、布局与访问**
**1. 对象的创建过程**
:    (1) 虚拟机遇到new指令，先检查这个指令的参数是否在常量池中定位到一个类的符号引用，并检查这个符号引用代表的类是否已经被加载、解析和初始化过。如果没有，先执行类加载。
:    (2) 为对象进行内存分配（具体方式由`Java`堆是否规整决定，即由垃圾收集器是否带有压缩整理功能决定）
>(a) **指针碰撞**——所有分配的内存放在一边，空闲的内存放在另一边，中间放一个指针作为分界点的指示器。对象分配内存就是把指针往空闲区域那边挪动一段与对象大小相等的距离。
>(b)**空闲列表**——虚拟机维护一个列表，记录哪块内存块可用，在分配的时候从列表中选取一块足够大的空间分配给对象实例，并更新列表上的记录。
**注意内存分配同步问题：**(a)对分配内存空间的动作进行同步(`CAS(Compare And Swap)`+失败重试) (b) `TLAB`(本地线程分配缓冲)
:     (3) 初始化。对象的内存空间分配为零值。
:     (4) 设置对象头(对象的哈希码、对象的`GC`分代年龄等)
:     (5) `<init>`方法执行，设置对象字段值(按照程序员的意愿)。 

**2. 对象的内存布局**

- **对象头**
- **实例数据**
- **对齐填充**

	**(1) 对象头**
	> (a)第一部分用于存储对象自身的运行时数据，如哈希码、`GC`分代年龄、锁状态标志、线程持有的锁等，成`为Mark Word`。
	>(b)第二部分是类型指针，对象指向它的类元数据的指针。(`-XX:+UseCompressedOops`) 
32位     | 64位(未开启指针压缩) | 64位(开启指针压缩)
-------- | -------- |--------
Mark Word(4 Byte)| Mark Word(8 Byte)| Mark Word(8 Byte)
Class 指针(4 Byte)|Class 指针(8 Byte) |Class 指针(4 Byte)
	   
:   **(2) 实例数据**
		实例数据是对象真正存储的有效信息，是程序中所定义的各种类型字段的内容。这部分内容的**存储顺序受到虚拟机分配策略和字段在`Java`中定义的顺序有关。**
		默认分配策略是**按照类型宽度由大到小，顺序排列，同时相同宽度的放在一起，最后是引用类型。**父类中定义的变量放在子类之前。若`-XX:FieldAllocationStyle=0`，则**引用类型放在最前面**。

:   **(3) 对齐填充(占位符)**
	 `HotSpot VM`**自动内存管理系统要求对象起始地址是8字节的整数倍**，也就是**对象大小必须是8字节的整数倍**。同时，**对象头是8字节的整数倍(1倍或2倍)**，如果实例数据没有对齐，则需要通过对其填充来补充。
				 
**3. 对象的访问定位**

- 句柄访问
- 直接指针

:   **(1) 句柄**
		采用句柄方式，`Java`堆中会有一块区域作为句柄池，`Java`栈中`reference`存储的是对象的句柄地址。而句柄中包含了对象实例数据与类型数据的各自地址信息。
		![句柄](http://img.blog.csdn.net/20170506113641413?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveHVyYW56eXk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
:   **(2) 直接指针**	
		`reference`存储的值直接是对象地址。
		![直接指针](http://img.blog.csdn.net/20170506113819492?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQveHVyYW56eXk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
:   **(3) 比较**	
		采用句柄访问的好处是`reference`中存储的是稳定的句柄地址，在对象移动时只改变句柄中对象实例的地址，`reference`不需要修改。
		采用直接指针，速度快，节省了一次指针定位的开销。

###**三、`OutOfMemoryError`异常**
目标是定位`OOM`异常出现的区域，知道为什么导致该异常，以及处理办法。

:   **(1) `Java`堆溢出**	

```
/**
 * VM Args：-Xms20m -Xmx20m（设置堆初始大小、最大大小）     
 * -XX:+HeapDumpOnOutOfMemoryError（设置堆转储快照）
 */
public class HeapOOM {

	static class OOMObject {
	}

	public static void main(String[] args) {
		List<OOMObject> list = new ArrayList<OOMObject>();

		while (true) {
			list.add(new OOMObject());
		}
	}
}
```
原因可以分为内存泄漏和内存溢出，对于内存泄漏，可以通过`Eclipse Memory Analzer`分析堆转储文件，分析泄露对象的信息和GC Roots引用链信息，就可以定位泄露代码的位置。  如果是内存溢出，设置`-Xms -Xmx`.

:   **(2) `Java`虚拟机栈、本地方法栈溢出**	
		栈容量由`-Xss`参数设定。
		
:   **(3) 方法区和运行时常量池溢出**	
	(1)`JDK1.6` `-XX:PermSize   -XX:MaxPermSize`限制方法区大小。
	
```
/**
 * VM Args：-XX:PermSize=10M -XX:MaxPermSize=10M
 */
public class RuntimeConstantPoolOOM {

	public static void main(String[] args) {
		// 使用List保持着常量池引用，避免Full GC回收常量池行为
		List<String> list = new ArrayList<String>();
		// 10MB的PermSize在integer范围内足够产生OOM了
		int i = 0; 
		while (true) {
			list.add(String.valueOf(i++).intern());
		}
	}
}
```

:   **(4) 本机直接内存溢出**		
		`DirectMemory`容量可通过`-XX:MaxDirectMemorySize`指定，如果不指定，则默认与`Java`堆(`-Xmx`)一样。 