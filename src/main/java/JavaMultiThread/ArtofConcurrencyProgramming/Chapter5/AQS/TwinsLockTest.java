package JavaMultiThread.ArtofConcurrencyProgramming.Chapter5.AQS;

import JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1.SleepUtils;
import org.junit.Test;

/**
 * TwinsLock Test (共享锁测试)
 *
 * @author xuanjian.xuwj
 */
public class TwinsLockTest {

	@Test
	public void test() {
		final TwinsLock lock = new TwinsLock();

		class Worker extends Thread {
			@Override
			public void run() {
				while (true) {
					lock.lock();
					try {
						SleepUtils.second(1);
						System.out.println(Thread.currentThread().getName());
						SleepUtils.second(1);
					} finally {
						lock.unlock();
					}
				}
			}
		}

		for (int i = 0; i < 10; i++) {
			Worker worker = new Worker();
			worker.setDaemon(true);
			worker.start();
		}

		for (int i = 0; i < 10; i++) {
			SleepUtils.second(1);
			System.out.println();
		}
	}

}