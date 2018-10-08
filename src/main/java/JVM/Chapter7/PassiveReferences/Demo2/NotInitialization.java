package JVM.Chapter7.PassiveReferences.Demo2;

/**
 * 通过数组定义引用类，不会触发此类的初始化
 */
public class NotInitialization {
    public static void main(String[] args) {
         SuperClass[] sca = new SuperClass[10];
    }
}
