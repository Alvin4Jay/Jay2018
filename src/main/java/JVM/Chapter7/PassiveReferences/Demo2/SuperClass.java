package JVM.Chapter7.PassiveReferences.Demo2;

/**
 *  通过数组定义引用类，不会触发此类的初始化
 */
public class SuperClass {
    static {
        System.out.println("SuperClass init!");
    }
    public static int value = 123;
    private String name = "ssss";
}
