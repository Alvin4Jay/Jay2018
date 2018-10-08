package JVM.Chapter7;

/**
 *  非法向前引用变量
 */
public class Test {
    static {
        i = 0;  //给变量赋值可以正常通过
        //System.out.println(i);  //这句编译器会提示"非法向前引用"
    }

    static int i = 1;
}
