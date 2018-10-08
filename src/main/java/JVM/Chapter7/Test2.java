package JVM.Chapter7;

/**
 * Created by Jay on 2017/5/31.
 */
public class Test2 {


    static class Parent{
        public static int A = 1;
        static {
            A = 2;
        }
    }

    static class SUb extends Parent{
        public static int B = A;
    }

    public static void main(String[] args) {
        System.out.println(SUb.B);  //2 父类先初始化
    }
}
