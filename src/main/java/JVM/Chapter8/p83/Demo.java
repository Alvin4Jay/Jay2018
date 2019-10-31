package JVM.Chapter8.p83;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * MethodType
 */
public class Demo {
    public static void main(String[] args) throws Throwable {
        //方法类型（返回类型，参数）
        MethodType mt = MethodType.methodType(String.class, int.class, int.class);

        //方法句柄
        MethodHandle mh = MethodHandles.lookup()
                .findVirtual(String.class, "substring", mt).bindTo("1234"); //绑定调用对象

        //调用
        System.out.println(mh.invoke(1, 3));

        int[][][] array = new int[1][2][-1];

    }
}
