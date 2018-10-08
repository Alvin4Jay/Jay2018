package JVM.Chapter7.PassiveReferences.Demo3;

/**
 * 引用常量池中的内容
 */
public class NotInitialization {
    public static void main(String[] args) {
        System.out.println(ConstClass.HELLOWORLD);
    }
}
