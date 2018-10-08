#Java基础面试题1

###1.Java变量
1. 局部变量
2. 类变量（静态变量）-- 属于类
3. 成员变量（非静态变量）-- 属于对象

###2.枚举
```java
enum Demo.CatEnum{
    /**
     * 1、带有构造方法的枚举，构造方法为只能为private(默认可不写private)；
     * 2、含带参构造方法的枚举，枚举值必须赋值；
     * 3、枚举中有了其他属性或方法之后，枚举值必须定义在最前面，且需要在最后一个枚举值后面加分号";"
     */
    BMW("宝马",1000000),
    JEEP("吉普",800000),
    MINI("mini",200000);

    private String name;
    private int price;

    Demo.CatEnum(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
```

###3访问控制修饰符
1. private 私有，同类中可见
2. default 同一包下可访问
3. protected 同一包和所有子类可访问
4. public 对所有类可见

###4GBK与UTF-8编码的转换
实现GBK编码字节流到UTF-8编码字节流的转换：byte[] src,dst---->
`dst=new String(src，"GBK").getBytes("UTF-8")` 先解码再编码

###5 try catch finally执行顺序问题
1. try catch中只要有finally语句都要执行（有特例：如果try 或 catch 里面有 System.exit（0）就不会执行finally了）；
2. finally语句在try或catch中的return语句执行之后返回之前执行，且finally里的修改语句不能影响try或catch中 return已经确定的返回值；
若finally里也有return语句则覆盖try或catch中的return语句直接返回；
3. 在遵守第（2）条return的情况下，执行顺序是：try-->catch（如果有异常的话）-->finally；

###6 静态代码块、子类、父类初始化顺序
执行顺序：1.静态代码块 --> 2.普通代码块 --> 3.构造方法
需要明白的是，1是类级别的，2和3是实例级别的，所以在父子类关系中，上述的执行顺序为：

父类静态代码块-->子类静态代码块-->父类普通代码块-->父类构造方法-->子类代码块-->子类构造方法；

也就是上到下（父类到子类）先走完 类级别的（静态的）--> 再依次走完父类的所有实例级别代码 --> 再走子类所有实例级别代码

###7 Final修饰符、volatile修饰符
1. Final变量（引用不能变）
  被声明为final的对象的引用不能指向不同的对象。但是final对象里的数据可以被改变。也就是说final对象的引用不能改变，但是里面的值可以改变
2. Final修饰的方法
  Final修饰的方法可以被子类继承，但是不能被子类修改（重写）。
3. Volatile 
   volatile修饰符，Volatile修饰的成员变量在每次被线程访问时，
   都强迫从共享内存中重读该成员变量的值。而且，当成员变量发生变化时，
   强迫线程将变化值回写到共享内存。这样在任何时刻，两个不同的线程总是
   看到某个成员变量的同一个值。
   
###8 可变参数
   一个方法中只能指定一个可变参数，它必须是方法的最后一个参数