package JDKSourceFileAnalysis.JUCLocks.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Unsafe实例不能通过getUnsafe()方法直接获取, 可通过反射获取
 *
 * @author xuanjian.xuwj
 */
public class UnsafeAcquireTest {
	public static void main(String[] args) throws Exception {
		// 不能直接获取，抛出异常 java.lang.SecurityException: Unsafe
		// Unsafe unsafe = Unsafe.getUnsafe();

		// 可以通过反射获取
		Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
		theUnsafe.setAccessible(true);
		// Unsafe unsafe = (Unsafe) theUnsafe.get(Unsafe.class);
		 Unsafe unsafe = (Unsafe) theUnsafe.get(null);
		System.out.println(unsafe);
	}
}
