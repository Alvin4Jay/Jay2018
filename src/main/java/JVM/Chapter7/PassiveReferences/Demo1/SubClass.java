package JVM.Chapter7.PassiveReferences.Demo1;

/**
 * 通过子类引用父类的静态字段，不会导致子类的初始化
 */
public class SubClass extends SuperClass{
    static {
        System.out.println("SubClass init");
    }
}
