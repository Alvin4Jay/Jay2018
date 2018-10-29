package JavaMultiThread.ArtofConcurrencyProgramming.Chapter2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1.基于CAS实现的线程安全的计数器方法 CAS
 * 2.非线程安全的计数器 i++
 *
 * @author xuanjian.xuwj
 */
public class Counter {

	private AtomicInteger atomicI = new AtomicInteger(0);

	private int i = 0;

	/**
	 * 使用CAS实现的线程安全的计数器
	 */
	private void safeCount() {
		for (; ; ) {
			int i = atomicI.get();
			boolean success = atomicI.compareAndSet(i, ++i);
			if (success) {
				break;
			}
		}
	}

	/**
	 * 非线程安全的计数器
	 */
	private void unsafeCount() {
		i++;
	}

	public static void main(String[] args) {
		final Counter cas = new Counter();
		List<Thread> threads = new ArrayList<>(600);
		long start = System.currentTimeMillis();

		// 创建线程
		for (int i = 0; i < 100; i++) {
			Thread thread = new Thread(() -> {
				for (int j = 0; j < 10000; j++) {
					cas.safeCount();
					cas.unsafeCount();
				}
			});
			threads.add(thread);
		}

		// 启动线程
		for (Thread thread : threads) {
			thread.start();
		}

		// 等待所有线程执行完毕
//		for (Thread thread : threads) {
//			try {
//				thread.join();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}

		threads.forEach(thread -> {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});


		System.out.println("atomicI: " + cas.atomicI.get());
		System.out.println("i: " + cas.i);
		System.out.println("time: " + (System.currentTimeMillis() - start));
	}

}
