package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$2;

import JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1.SleepUtils;

/**
 * 中断测试，查看中断标志位
 *
 * @author xuanjian.xuwj
 */
public class InterruptedTest {

	public static void main(String[] args) {

		Thread sleepThread = new Thread(new SleepRunner(), "SleepRunner");
		sleepThread.setDaemon(true);

		Thread busyThread = new Thread(new BusyRunner(), "BusyRunner");
		busyThread.setDaemon(true);

		sleepThread.start();
		busyThread.start();
		// 等待5s
		SleepUtils.second(5);
		// 触发中断
		sleepThread.interrupt();
		busyThread.interrupt();

		// sleep线程 抛出异常，清除中断标志位，线程正常运行
		System.out.println("SleepThread interrupted is " + sleepThread.isInterrupted());
		// 中断标志位置位，不会被清除，线程正常运行
		System.out.println("BusyThread interrupted is " + busyThread.isInterrupted());

		SleepUtils.second(25);
	}

	// 一直睡眠
	static class SleepRunner implements Runnable {
		private static int num = 0;

		@Override
		public void run() {
			while (true) {
				SleepUtils.second(10);
				System.out.println("Haha " + num++);
			}
		}
	}

	// 一直执行
	static class BusyRunner implements Runnable {
		@Override
		public void run() {
			while (true) {

			}
		}
	}

}
