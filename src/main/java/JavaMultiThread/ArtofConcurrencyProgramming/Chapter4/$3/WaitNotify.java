package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$3;

import JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1.SleepUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Wait-Notify 机制
 *
 * @author xuanjian.xuwj
 */
public class WaitNotify {
	// 条件
	private static boolean flag = true;
	// 对象锁
	private static Object lock = new Object();

	private static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

	public static void main(String[] args) {
		Thread waitThread = new Thread(new Wait(), "WaitThread");
		Thread notifyThread = new Thread(new Notify(), "NotifyThread");
		waitThread.start();

		SleepUtils.second(5);

		notifyThread.start();
	}

	// 等待线程
	private static class Wait implements Runnable {
		@Override
		public void run() {
			// 获取对象的监视器锁
			synchronized (lock) {
				// 条件不满足，等待，并释放锁
				while (flag) {
					try {
						System.out.println(Thread.currentThread().getName() + " wait at " + format.format(new Date()));
						lock.wait(); // 在这里等待
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				// 被notify，条件满足，重新获取到对象监视器锁，继续执行后续的任务
				System.out.println(Thread.currentThread().getName() + " done at " + format.format(new Date()));
			}
		}
	}

	// 通知线程
	private static class Notify implements Runnable {
		@Override
		public void run() {
			// 先获取对象的监视器锁
			synchronized (lock) {
				// 改变条件
				flag = false;
				// 通知。不会马上释放lock的锁，需要当前线程释放了锁之后，等待线程才可能从wait()方法返回
				lock.notify();
				System.out.println(Thread.currentThread().getName() + " notify at " + format.format(new Date()));
				SleepUtils.second(5); // 睡眠之后释放锁
			}
		}
	}

}
