package JavaMultiThread.ArtofConcurrencyProgramming.Chapter10.ScheduledThreadPoolExecutor;

import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ScheduledExecutorServiceTest
 *
 * @author xuanjian.xuwj
 */
public class ScheduledExecutorServiceTest {
	public static void main(String[] args) {
		ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2,
				new CustomThreadFactory());

		System.out.println(new Date() + ": task begin...");
		scheduledThreadPoolExecutor.scheduleWithFixedDelay(() -> {
			try {
				System.out.println(new Date());
				TimeUnit.SECONDS.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, 2, 3, TimeUnit.SECONDS);
	}

	private static class CustomThreadFactory implements ThreadFactory {
		private static final AtomicLong SEQUENCE = new AtomicLong(0);

		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("test-scheduled-thread" + SEQUENCE.getAndIncrement());
			thread.setDaemon(false);
			return thread;
		}
	}
}
