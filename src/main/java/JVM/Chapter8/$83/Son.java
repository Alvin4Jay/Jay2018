package JVM.Chapter8.$83;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 *
 */
public class Son extends Father {
    void thinking() {

        try {
            MethodType mt = MethodType.methodType(void.class);

            MethodHandle mh = MethodHandles.lookup().findSpecial(GrandFather.class, "thinking", mt, this.getClass());

            mh.invoke(this);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new Son().thinking();
    }
}

class GrandFather {
    void thinking() {
        System.out.println("i am grandfather");
    }
}

class Father extends GrandFather {
    void thinking() {
        System.out.println("i am father");
    }
}

