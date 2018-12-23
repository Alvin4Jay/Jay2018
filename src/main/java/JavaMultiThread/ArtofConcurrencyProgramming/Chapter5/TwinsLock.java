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

		@Override
		protected int tryAcquireShared(int arg) {
			for (; ; ) {
				int nowCount = getState();
				int newCount = nowCount - arg;
				if (newCount < 0) {
					return -1;
				}
				if (compareAndSetState(nowCount, newCount)) {
					return newCount;
				}
			}
		}

		@Override
		protected boolean tryReleaseShared(int arg) {
			for (; ; ) {
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
