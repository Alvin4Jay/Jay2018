package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$2;

import JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1.SleepUtils;

/**
 * 正确的关闭线程
 *
 * @author xuanjian.xuwj
 */
public class CorrectShutdownTest {
	public static void main(String[] args) {
		Runner one = new Runner();
		Thread countThread = new Thread(one, "CountThreadOne");
		countThread.start();

		SleepUtils.second(1);
		// 根据中断状态终止线程
		countThread.interrupt();

		Runner two = new Runner();
		Thread countThread2 = new Thread(two, "CountThreadTwo");
		countThread2.start();

		SleepUtils.second(1);
		// 根据volatile boolean变量终止线程
		two.cancel();
	}

	/**
	 * 计数线程
	 */
	private static class Runner implements Runnable {
		private int count = 0;
		/** 开关变量 */
		private volatile boolean on = true;

		@Override
		public void run() {
			// 根据中断标志位和volatile boolean变量进行判断，终止线程
			while (on && !Thread.currentThread().isInterrupted()) {
				count++;
			}
			System.out.println(Thread.currentThread().getName() + " Count: " + count);
		}

		public void cancel() {
			on = false;
		}
	}

}
