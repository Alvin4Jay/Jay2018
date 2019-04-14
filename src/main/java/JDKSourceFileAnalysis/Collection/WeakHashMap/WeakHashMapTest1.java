package JDKSourceFileAnalysis.Collection.WeakHashMap;

import java.util.*;

/**
 * JVM参数 -Xmx5M
 *
 * @author xuanjian
 */
public class WeakHashMapTest1 {
	public static void main(String[] args) {
		weakHashMapTestWithStrongRef();
		//		weakHashMapTest();
		//		hashMapTest();
	}


	/**
	 * WeakHashMap Entry的key被强引用，在内存不够时，不会释放内存
	 */
	private static void weakHashMapTestWithStrongRef() {
		// Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
		Map<Integer, Object> map = new WeakHashMap<>();
		List<Integer> list = new ArrayList<>(); // 强引用，无法回收entry
		for (int i = 0; i < 10000; i++) {
			Integer integer = i;
			list.add(integer);
			map.put(integer, new byte[i]);
		}
	}

	/**
	 * WeakHashMap key弱引用，在内存不够时，释放部分Entry内存
	 */
	private static void weakHashMapTest() {
		Map<Integer, Object> map = new WeakHashMap<>();
		for (int i = 0; i < 10000; i++) {
			Integer integer = i;
			map.put(integer, new byte[i]);
		}
	}

	/**
	 * HashMap强引用，无法释放Entry内存
	 */
	private static void hashMapTest() {
		// Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
		Map<Integer, Object> map = new HashMap<>();
		for (int i = 0; i < 10000; i++) {
			Integer integer = i;
			map.put(integer, new byte[i]);
		}
	}

}
