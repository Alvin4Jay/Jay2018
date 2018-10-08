package JVM.Chapter8.$83;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Method Handle基础使用方法
 */
public class MethodHandleTest {
    static class ClassA{
        public void println(String s){
            System.out.println(s);
        }
    }

    public static void main(String[] args) throws Throwable {
        //方法持有者
        Object obj = System.currentTimeMillis() % 2 == 0? System.out : new ClassA();
        //获取方法句柄，调用（不用指定方法所属对象，bingTo）
        getPrintMH(obj).invokeExact("test");

    }

    private static MethodHandle getPrintMH(Object receiver) throws Throwable  {
        //方法参数
        MethodType mt = MethodType.methodType(void.class, String.class);
        //方法持有者实际类型
        System.out.println(receiver.getClass());
        //获取句柄
        return MethodHandles.lookup()
                .findVirtual(receiver.getClass(), "println", mt)
                .bindTo(receiver); //绑定对象

    }
}
