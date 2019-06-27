# String类问题答疑

### 1. String为什么不可变？

```java
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
    /** The value is used for character storage. */
    private final char value[];

    /** Cache the hash code for the string */
    private int hash; // Default to 0
    // ...
}
```

​	首先String类声明为final，则该类不能被其他类继承；其次String底层存储数据的是char[]数组，该数组是final的，则String在构造时必须初始化char[]数组，使得char[] value变量不能再指向其他数组(value数组中的元素值还是可变的)。但即使char[] value声明为final，其值也是可以改变的，比如通过反射。

​	**真正使得String不可变的原因**是①String类中的方法都是返回新的String对象，没有对原char[] value进行更改(copy)；②没有暴露内部成员字段，没有暴露方法去修改；③String设计成final，禁止继承，避免被其他类继承后破坏。所以**String是不可变的关键都在底层的实现，而不是一个final。**考验的是JDK工程师构造数据类型，封装数据的功力。

### 2. hashCode()方法计算hash值时，为什么基数是31？

- ① 31是一个不大不小的质数，是作为 hashCode 乘子的优选质数之一。使用31，计算出来的hash值分布比较广(均匀)，能够较好地避免出现hash冲突。参考:
  - [科普：为什么 String hashCode 方法选择数字31作为乘子](https://segmentfault.com/a/1190000010799123)
  - [Why does Java's hashCode() in String use 31 as a multiplier?](https://stackoverflow.com/questions/299304/why-does-javas-hashcode-in-string-use-31-as-a-multiplier)
- ② 31可以被现代 JVM 优化，`31 * i = (i << 5) - i`。(移位和减法-1)

### 3. String.intern()方法作用

​	String常量池的主要使用方法有两种：

- 直接使用双引号声明出来的`String`对象会直接存储在常量池中。

- 如果不是用双引号声明的`String`对象(new出来的，存放在堆中)，可以使用`String`提供的`intern`方法。intern 方法会从字符串常量池中查询当前字符串是否存在，若不存在就会将当前字符串放入常量池中。

  > JDK:
  >
  > “如果常量池中存在当前字符串, 就会直接返回当前字符串. 如果常量池中没有此字符串, 会将此字符串放入常量池中后, 再返回”。

参考: [深入解析String#intern](https://tech.meituan.com/2014/03/06/in-depth-understanding-string-intern.html)

### 4. 字符串对象创建个数问题

(1)

```java
private static void test01() {

    String str1 = new String("A" + "B"); // 会创建多少个对象? 总共2，常量池1--"AB"，堆内存1--new String("AB")
    
    String str2 = new String("ABC") + "ABC"; // 会创建多少个对象? 总共常量池1--"ABC"，堆内存3--new String("ABC")/new StringBuilder()/StringBuilder.toString()
    
}
```

(2)

```java
private static void test01() {

    String str = "hello";
    String str2 = "hel" + "lo"; // 这个是在编译的时候确定的，会在字符串常量池中创建一个hello对象，然后把对象的引用给str。
    System.out.println(str == str2); // true
    System.out.println(str.equals(str2)); // 始终比较的是值是否相等返回 true

    String str3 = "hel" + new String("lo"); // 运行时创建，在堆内存
    System.out.println(str == str3); // false
    System.out.println(str.equals(str3)); // true

    String str4 = new String("hello"); // new String("llo");这个是在运行的时候确定，所以这个会在堆中的字符创非常量池中去创建一个hello，返回引用，此时str == str3返回的就是false。
    System.out.println(str == str4); // false。在堆中存放的对象，只要有new就会是新的创建对象
    System.out.println(str.equals(str4));//true

    String str5 = "hel" + new String("lo"); // 有new就是新的
    String str6 = "hel" + new String("lo"); // 虽然这两个都是在堆的非常量池中动态创建，但是都是动态的，new String()所以不是使用同一份。
    System.out.println(str5 == str6); // false
    System.out.println(str5.equals(str6)); // true

    String str7 = new String("hello"); // 有new就是新的
    String str8 = new String("hello"); // 虽然这两个都是在堆的非常量池中动态创建，但是都是动态的，new String()所以不是使用同一份。
    System.out.println(str7 == str8); // false
    System.out.println(str7.equals(str8)); // true
}
```

(3)

```java
String s=new String(“str”)+new String("ing") // 总共6个对象
// 常量池2个："str" 和 "ing"
// 堆内存：2个String对象+1个StringBuilder对象+1个StringBuilder.toString()生成的String对象  
```

参考：[请问下题中创建几个String对象？](https://www.zhihu.com/question/64726158/answer/223549872)

### 参考文献

- [Java 7 源码学习系列（一）——String](https://www.hollischuang.com/archives/99)
- [String源码分析](https://juejin.im/post/59fffddc5188253d6816f9c1)

- [字符串常量池和堆中的非常量池（堆）创建字符串（new 和非new穿件 == 和equal比较）](https://blog.csdn.net/qq_28081081/article/details/80467181)

- [在java中String类为什么要设计成final？](https://www.zhihu.com/question/31345592)