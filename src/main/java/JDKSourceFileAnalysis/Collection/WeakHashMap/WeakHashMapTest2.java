package JDKSourceFileAnalysis.Collection.WeakHashMap;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JVM参数 -Xmx5M -XX:+PrintGCDetails
 *
 * @author xuanjian
 */
public class WeakHashMapTest2 {
	public static void main(String[] args) throws InterruptedException {
		nullKeyWithBigObject2();
		//		nullKeyWithBigObject();
	}

	/**
	 * WeakHashMap (null key---大对象)，显示调用remove(null)方法
	 */
	private static void nullKeyWithBigObject2() throws InterruptedException {
		Map<Integer, Object> map = new WeakHashMap<>();
		System.gc();
		System.out.println("===========gc:1=============");
		map.put(null, new byte[5 * 1024 * 600]);
		TimeUnit.SECONDS.sleep(5);
		System.gc();
		System.out.println("===========gc:2=============");
		TimeUnit.SECONDS.sleep(5);
		System.gc();
		System.out.println("===========gc:3=============");
		map.remove(null);
		TimeUnit.SECONDS.sleep(5);
		System.gc();
		System.out.println("===========gc:4=============");

	}

	/**
	 * WeakHashMap (null key---大对象)
	 */
	private static void nullKeyWithBigObject() throws InterruptedException {
		Map<Object, Object> map = new WeakHashMap<>();
		map.put(null, new byte[5 * 1024 * 600]);
		int i = 1;
		while (true) {
			System.out.println();
			TimeUnit.SECONDS.sleep(2);
			System.out.println(map.size());
			System.gc();
			System.out.println("==============第" + i + "次GC结束=============");
			i++;
		}
	}

}
