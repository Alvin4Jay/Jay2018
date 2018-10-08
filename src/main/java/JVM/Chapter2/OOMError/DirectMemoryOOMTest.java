package JVM.Chapter2.OOMError;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 *  直接内存OOM
 *  VM Args: -Xmx20M -XX:MaxDirectMemorySize=10M
 *
 */
public class DirectMemoryOOMTest {
    private static final int _1MB = 1024*1024;

    public static void main(String[] args) throws Exception {

        Field unsafeField  = Unsafe.class.getDeclaredFields()[0];
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);

        while(true){
            unsafe.allocateMemory(_1MB);
        }
    }
}