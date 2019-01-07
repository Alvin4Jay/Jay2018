package JavaMultiThread.ArtofConcurrencyProgramming.Chapter5;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 允许同一时刻，两个线程获取锁的共享锁
 *
 * @author xuanjian.xuwj
 */
public class TwinsLock {
	// 最大同时获取锁的线程数 2
	private final Sync sync = new Sync(2);

	private static class Sync extends AbstractQueuedSynchronizer {

		Sync(int count) {
			if (count <= 0) {
				throw new IllegalArgumentException("count must large than 0");
			}
			setState(count);
		}

		// 返回正数，获取成功，后续操作可能成功；返回0，获取成功，后续的获取会失败；返回负数，获取失败。
		@Override
		protected int tryAcquireShared(int arg) {
			// 自旋+CAS
			for (;;) {
				int nowCount = getState();
				int newCount = nowCount - arg;
				if (newCount < 0) {
					// 失败
					return -1;
				}
				if (compareAndSetState(nowCount, newCount)) {
					// 大于等于0
					return newCount;
				}
			}
		}

		@Override
		protected boolean tryReleaseShared(int arg) {
			// 自旋+CAS
			for (;;) {
				int nowCount = getState();
				int newCount = nowCount + arg;
				if (compareAndSetState(nowCount, newCount)) {
					return true;
				}
			}
		}
	}

	public void lock() {
		sync.acquireShared(1);
	}

	public void lockInterruptibly() throws InterruptedException {
		sync.acquireSharedInterruptibly(1);
	}

	public void unlock() {
		sync.releaseShared(1);
	}

}
