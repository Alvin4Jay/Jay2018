# 理解Java对象序列化机制

## 一、Java对象序列化概述

>  ​	序列化是一种对象持久化的手段。普遍应用在网络传输、RMI(远程方法调用)等场景中。
>
>  ​	Java平台允许我们在内存中创建可复用的Java对象，但一般情况下，只有当JVM处于运行时，这些对象才可能存在，即这些对象的生命周期不会比JVM的生命周期更长。但在现实应用中，就可能要求在JVM停止运行之后能够保存(持久化)指定的对象，并在将来重新读取被保存的对象。Java对象序列化就能够帮助我们实现该功能。
>
>  ​	使用Java对象序列化，在保存对象时，会把其状态保存为一组字节，在未来再将这些字节组装成对象。必须注意的是，对象序列化保存的是对象的"状态"，即它的成员变量。由此可知，对象序列化不会关注类中的静态变量(static)。
>
>  ​	 除了在持久化对象时会用到对象序列化之外，当使用RMI(远程方法调用)，或在网络中传递对象时，都会用到对象序列化。Java序列化API为处理对象序列化提供了一个标准机制，该API非常简单易用。	

## 二、对象序列化举例

###1.序列化对象

```java
import java.io.Serializable;
import java.util.Date;

/**
 * Employee
 */
public class Employee implements Serializable {
    // 序列化版本标识
    private static final long serialVersionUID = -622419301692189579L;
	// transient修饰的变量不会被序列化
    private transient String name;
    private double salary;
    private Date hireDay;

    public Employee() {
        System.out.println("无参构造器...");
    }

    public Employee(String name, double salary, Date hireDay) {
        System.out.println("带参构造器...");
        this.name = name;
        this.salary = salary;
        this.hireDay = hireDay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public Date getHireDay() {
        return hireDay;
    }

    public void setHireDay(Date hireDay) {
        this.hireDay = hireDay;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", salary=" + salary +
                ", hireDay=" + hireDay +
                '}';
    }

}
```

###2.序列化处理程序

```java
import java.io.IOException;
import java.util.Date;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Serialization	
 */
public class SerializationTest {
    public static void main(String[] args) {
        Employee harry = new Employee();
        harry.setName("Harry");
        harry.setSalary(10000);
        harry.setHireDay(new Date());
        System.out.println(harry);

        // save object to file
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("employee.dat"))) {
            out.writeObject(harry);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get object data from file
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("employee.dat"))) {
            Employee newHarry = (Employee) in.readObject();
            System.out.println(newHarry);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
/*
out: 
    无参构造器...
    Employee{name='Harry', salary=10000.0, hireDay=Tue Sep 18 13:25:08 CST 2018}
	Employee{name='null', salary=10000.0, hireDay=Tue Sep 18 13:25:08 CST 2018} 
*/
```

​		从上面的结果可以看出，类`Employee`实现`java.io.Serializable`接口，并通过`ObjectOutputStream`与`ObjectInputStream`进行对象的序列化和反序列化过程。注意：<u>在重新读取对象数据时，没有调用类`Employee`任何的构造器，像是直接使用字节将`Employee`对象还原出来的</u>。

### 3.序列化要点

- 序列化类需实现`java.io.Serializable`接口，才能被序列化。

  ```java
  // 以下是 ObjectOutputStream 的 writeObject0 序列化方法的一部分代码，可看出如果被写对象的类型是String，或数组Array，或Enum，或实现Serializable接口，那么就可以对该对象进行序列化，否则将抛出NotSerializableException。
  
  // remaining cases
  if (obj instanceof String) {
      writeString((String) obj, unshared);
  } else if (cl.isArray()) {
      writeArray(obj, desc, unshared);
  } else if (obj instanceof Enum) {
      writeEnum((Enum<?>) obj, desc, unshared);
  } else if (obj instanceof Serializable) { // 实现java.io.Serializable接口
      writeOrdinaryObject(obj, desc, unshared);
  } else {
      if (extendedDebugInfo) {
          throw new NotSerializableException(
              cl.getName() + "\n" + debugInfoStack.toString());
      } else {
          throw new NotSerializableException(cl.getName());
      }
  }
  ```

- 通过`ObjectOutputStream`和`ObjectInputStream`对对象进行序列化及反序列化。

- 虚拟机是否允许反序列化，不仅取决于类路径和功能代码是否一致，一个非常重要的一点是两个类的序列化 ID 是否一致（就是 `private static final long serialVersionUID`）。

- 序列化并不保存静态变量(static变量)。

- 要想将父类对象也序列化，就需要让父类也实现`Serializable` 接口。

- `transient` 关键字的作用是控制变量的序列化，在变量声明前加上该关键字，可以阻止该变量被序列化到文件中，在被反序列化后，`transient `变量的值被设为初始值，如 `int` 型的是 0，对象型的是` null`（如上述测试结果所示）。

- 如果仅仅只是让某个类实现`Serializable`接口，而没有其它任何处理的话，则就是使用**默认序列化机制**。使用默认机制，在序列化对象时，不仅会序列化当前对象本身，还会对该对象引用的其它对象也进行序列化，同样地，这些其它对象引用的另外对象也将被序列化，以此类推。所以，如果一个对象包含的成员变量是容器类对象，而这些容器所含有的元素也是容器类对象，那么这个序列化的过程就会较复杂，开销也较大。

##三、自定义序列化策略

### 1.`transient`关键字

​	上述示例已说明，name属性未被序列化。

###2.`writeObject()`方法与`readObject()`方法	

```java
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;

/**
 * Employee
 */
public class Employee implements Serializable {
	// 序列化版本
    private static final long serialVersionUID = -622419301692189579L;
    // transient修饰的变量不会被序列化
    private transient String name;
    private double salary;
    private Date hireDay;

    public Employee() {
        System.out.println("无参构造器...");
    }

    public Employee(String name, double salary, Date hireDay) {
        System.out.println("带参构造器...");
        this.name = name;
        this.salary = salary;
        this.hireDay = hireDay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public Date getHireDay() {
        return hireDay;
    }

    public void setHireDay(Date hireDay) {
        this.hireDay = hireDay;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", salary=" + salary +
                ", hireDay=" + hireDay +
                '}';
    }
	// 自定义序列化策略
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(name);
    }
	// 自定义反序列化策略
    private void readObject(java.io.ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        name = ois.readUTF();
    }

}
/*
out:
	无参构造器...
    Employee{name='Harry', salary=10000.0, hireDay=Tue Sep 18 19:45:38 CST 2018}
    Employee{name='Harry', salary=10000.0, hireDay=Tue Sep 18 19:45:38 CST 2018}
*/
```

​	`writeObject`和`readObject`是私有方法，这两个方法是在序列化和反序列化过程中通过**反射机制**调用的。

从`ObjectOutputStream`调用自定义的`writeObject`方法的调用栈如下：

```java
writeObject ---> writeObject0 --->writeOrdinaryObject--->writeSerialData--->invokeWriteObject
```

```java
private void writeSerialData(Object obj, ObjectStreamClass desc)
        throws IOException
    {
        ObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
        for (int i = 0; i < slots.length; i++) {
            ObjectStreamClass slotDesc = slots[i].desc;
            // 存在自定义的writeObject()方法
            if (slotDesc.hasWriteObjectMethod()) {
                PutFieldImpl oldPut = curPut;
                curPut = null;
                SerialCallbackContext oldContext = curContext;

                if (extendedDebugInfo) {
                    debugInfoStack.push(
                        "custom writeObject data (class \"" +
                        slotDesc.getName() + "\")");
                }
                try {
                    curContext = new SerialCallbackContext(obj, slotDesc);
                    bout.setBlockDataMode(true);
                    // 反射调用
                    slotDesc.invokeWriteObject(obj, this);
                    bout.setBlockDataMode(false);
                    bout.writeByte(TC_ENDBLOCKDATA);
                } finally {
                    curContext.setUsed();
                    curContext = oldContext;
                    if (extendedDebugInfo) {
                        debugInfoStack.pop();
                    }
                }

                curPut = oldPut;
            } else {
                defaultWriteFields(obj, slotDesc);
            }
        }
    }

/** class-defined writeObject method, or null if none */
private Method writeObjectMethod;

void invokeWriteObject(Object obj, ObjectOutputStream out)
        throws IOException, UnsupportedOperationException
    {
        requireInitialized();
        if (writeObjectMethod != null) {
            try {
            	// 通过反射的方式调用writeObjectMethod方法
                writeObjectMethod.invoke(obj, new Object[]{ out });
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof IOException) {
                    throw (IOException) th;
                } else {
                    throwMiscException(th);
                }
            } catch (IllegalAccessException ex) {
                // should not occur, as access checks have been suppressed
                throw new InternalError(ex);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }
```

### 3.`Externalizable`接口

​		无论是使用`transient`关键字，还是使用`writeObject()`和`readObject()`方法，其实都是基于`Serializable`接口的序列化。`JDK`中提供了另一个序列化接口----`Externalizable`，使用该接口之后，之前基于`Serializable`接口的序列化机制就将失效。

```java
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * Employee
 */
public class Employee implements Externalizable {

    private static final long serialVersionUID = -622419301692189579L;
    private transient String name;
    private double salary;
    private Date hireDay;

    public Employee() {
        System.out.println("无参构造器...");
    }

    public Employee(String name, double salary, Date hireDay) {
        System.out.println("带参构造器...");
        this.name = name;
        this.salary = salary;
        this.hireDay = hireDay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public Date getHireDay() {
        return hireDay;
    }

    public void setHireDay(Date hireDay) {
        this.hireDay = hireDay;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", salary=" + salary +
                ", hireDay=" + hireDay +
                '}';
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(name);
    }

    private void readObject(java.io.ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        name = ois.readUTF();
    }
	
    // 重写方法
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // do nothing
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        // do nothing
    }
    
}
/*
out: 
	无参构造器...
    Employee{name='Harry', salary=10000.0, hireDay=Tue Sep 18 19:58:18 CST 2018}
    无参构造器...
    Employee{name='null', salary=0.0, hireDay=null}
*/
```

​		从上面可以看出，一方面可以看出**`Person`对象中任何一个字段都没有被序列化**。另一方面，可以发现这此次序列化过程**调用了`Person`类的无参构造器**。

​	 	`Externalizable`继承于`Serializable`，<u>当使用该接口时，序列化的细节需要由程序员去完成</u>。如上所示的代码，由于`writeExternal()`与`readExternal()`方法未作任何处理，那么该序列化行为将不会保存/读取任何一个字段。这也就是为什么输出结果中所有字段的值均为空。

​	另外，若使用`Externalizable`进行序列化，当读取对象时，会调用**被序列化类的无参构造器**去创建一个新的对象，然后再将被保存对象的字段的值分别填充到新对象中。这就是为什么在此次序列化过程中Person类的无参构造器会被调用。由于这个原因，实现`Externalizable`接口的类必须要提供一个无参的构造器，且**它的访问权限为`public`。**

​	修改上面的代码，序列化	`salary`、`name`属性，不序列化`hireday`属性，有如下的结果，符合设置的要求。

```java
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * Employee
 */
public class Employee implements Externalizable {

    private static final long serialVersionUID = -622419301692189579L;
    private transient String name;
    private double salary;
    private Date hireDay;

    public Employee() {
        System.out.println("无参构造器...");
    }

    public Employee(String name, double salary, Date hireDay) {
        System.out.println("带参构造器...");
        this.name = name;
        this.salary = salary;
        this.hireDay = hireDay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public Date getHireDay() {
        return hireDay;
    }

    public void setHireDay(Date hireDay) {
        this.hireDay = hireDay;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", salary=" + salary +
                ", hireDay=" + hireDay +
                '}';
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeUTF(name);
    }

    private void readObject(java.io.ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        name = ois.readUTF();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(name);
        out.writeDouble(salary);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        name = (String) in.readObject();
        salary = in.readDouble();
    }

}
/*
out:
	无参构造器...
    Employee{name='Harry', salary=10000.0, hireDay=Tue Sep 18 20:08:10 CST 2018}
    无参构造器...
    Employee{name='Harry', salary=10000.0, hireDay=null}
*/
```

### 4.`readResolve()`方法

​	在序列化和反序列化时，如果目标对象唯一，则必须小心，这通常会在**实现单例和类型安全的枚举**时发生<u>。如果使用了`Enum`结构，则不必担心序列化，能够正常工作</u>。但如果是遗留代码，其中包含如下的枚举类型：

```java
import java.io.Serializable;

/**
 * Single Object Serialization, 遗留代码中的枚举类
 */
public class Fruit implements Serializable {

    public static final Fruit APPLE = new Fruit(1);
    public static final Fruit BANANA = new Fruit(2);

    private int v;

    private Fruit(int v) {
        this.v = v;
    }

}
```

```java
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Enum Serialization 测试程序
 */
public class EnumTest {
    public static void main(String[] args){

        // save object to file
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("employee1.dat"))) {
            Fruit apple = Fruit.APPLE;
            out.writeObject(apple);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // get object data from file
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("employee1.dat"))) {
            Fruit newApple = (Fruit) in.readObject();
            // false, newApple是Fruit的一个全新对象，与任何预定义的常量都不相同。即使构造器私有，序列化机制也可以创建新对象。
            System.out.println(newApple == Fruit.APPLE);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
```

​		为修复该问题，需在`Fruit`类中定义`readResolve()`方法(特殊的序列化方法)。定义了该方法后，在对象反序列化之后就会调用该方法。它必须返回一个对象，该对象会成为`readObject()`的返回值。如下所示：

```java
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Single Object Serialization
 */
public class Fruit implements Serializable {

    public static final Fruit APPLE = new Fruit(1);
    public static final Fruit BANANA = new Fruit(2);

    private int v;

    private Fruit(int v) {
        this.v = v;
    }
	// 定义的返回值方法
    protected Object readResolve() throws ObjectStreamException{
        if (v == 1) {
            return APPLE;
        }
        if (v == 2) {
            return BANANA;
        }
        throw new ObjectStreamException(){};
    }

}
/*
out:
	true
*/
```

​	**无论是实现Serializable接口，或是Externalizable接口，当从I/O流中读取对象时，readResolve()方法都会被调用到。实际上就是用readResolve()中返回的对象直接替换在反序列化过程中创建的对象，而被创建的对象则会被垃圾回收掉。**



###参考文献

- [**理解Java对象序列化**](http://www.blogjava.net/jiangshachina/archive/2012/02/13/369898.html)
- [深入分析Java的序列化与反序列化](http://www.hollischuang.com/archives/1140)