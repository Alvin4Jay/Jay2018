package JVM.Chapter7.PassiveReferences.Demo1;

/**
 * 非主动使用类字段实例
 */
public class NotInitialization {
    public static void main(String[] args) {
        System.out.println(SubClass.value);  //子类不会初始化，但会加载
    }
}
