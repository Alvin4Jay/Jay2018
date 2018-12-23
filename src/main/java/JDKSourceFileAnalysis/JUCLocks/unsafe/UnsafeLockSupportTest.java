package JDKSourceFileAnalysis.JUCLocks.unsafe;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * LockSupport.park()/unpark() supported by Unsafe
 *
 * @author xuanjian.xuwj
 */
public class UnsafeLockSupportTest {
	public static void main(String[] args) throws InterruptedException {
		ThreadPark threadPark = new ThreadPark("ThreadPark");
		threadPark.start();

		TimeUnit.SECONDS.sleep(5);

		ThreadUnpark threadUnPark = new ThreadUnpark("ThreadUnpark", threadPark);
		threadUnPark.start();

		// main线程等待ThreadUnpark执行成功
		threadUnPark.join();
		System.out.println(Thread.currentThread().getName() + "--运行成功....");
	}

	private static class ThreadPark extends Thread {
		public ThreadPark(String name) {
			super(name);
		}

		@Override
		public void run() {
			System.out.println(Thread.currentThread().getName() + "--我将被阻塞在这了60s....");
			LockSupport.parkNanos(1000000000L * 60);
			System.out.println(Thread.currentThread().getName() + "--我被恢复正常了....");
		}
	}

	private static class ThreadUnpark extends Thread {
		public Thread thread;

		public ThreadUnpark(String name, Thread thread) {
			super(name);
			this.thread = thread;
		}

		@Override
		public void run() {
			System.out.println(Thread.currentThread().getName() + "--提前恢复阻塞线程ThreadPark");
			// 恢复阻塞线程
			LockSupport.unpark(thread);

			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
