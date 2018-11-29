package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$3;

import JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1.SleepUtils;

/**
 * ThreadLocal 测试
 *
 * @author xuanjian.xuwj
 */
public class ThreadLocalTest {

	// 如果get()方法调用之前未调用set()方法，则在调用get()时，会先调用initialValue()方法进行初始化
	// 每个线程调用一次
	private static final ThreadLocal<Long> TIME_THREADLOCAL = new ThreadLocal<Long>(){
		@Override
		protected Long initialValue() {
			return System.currentTimeMillis();
		}
	};

	public static void begin() {
		TIME_THREADLOCAL.set(System.currentTimeMillis());
	}

	public static long end() {
		return System.currentTimeMillis() - TIME_THREADLOCAL.get();
	}


	public static void main(String[] args){
		ThreadLocalTest.begin();

		SleepUtils.second(1);

		System.out.println("Cost: " + ThreadLocalTest.end() + " ms.");
	}
}
