package JavaMultiThread.ArtofConcurrencyProgramming.Chapter5.ReentrantLock;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 测试公平锁和非公平锁
 *
 * @author xuanjian.xuwj
 */
public class FairAndUnfairTest {
	private static ReentrantLock2 fairLock = new ReentrantLock2(true);
	private static ReentrantLock2 unfairLock = new ReentrantLock2(false);

	public static void main(String[] args) {
		//		testLock(fairLock);
		testLock(unfairLock);
	}

	private static void testLock(ReentrantLock2 lock) {
		for (int i = 0; i < 5; i++) {
			new Job(lock).start();
		}
	}

	private static class Job extends Thread {
		private ReentrantLock2 lock;

		public Job(ReentrantLock2 lock) {
			this.lock = lock;
		}

		@Override
		public void run() {
			while (true) {
				lock.lock();
				try {
					Collection<Thread> queuedThreads = lock.getQueuedThreads();
					System.out.println("Lock by " + Thread.currentThread().getName() + ", Waiting by " + Arrays
							.toString(queuedThreads.stream().map(Thread::getName).toArray(String[]::new)));
					// TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		}
	}

	private static class ReentrantLock2 extends ReentrantLock {
		public ReentrantLock2(boolean fair) {
			super(fair);
		}

		@Override
		protected Collection<Thread> getQueuedThreads() {
			List<Thread> queuedThreads = new ArrayList<>(super.getQueuedThreads());
			// 逆序
			Collections.reverse(queuedThreads);
			return queuedThreads;
		}
	}
}
