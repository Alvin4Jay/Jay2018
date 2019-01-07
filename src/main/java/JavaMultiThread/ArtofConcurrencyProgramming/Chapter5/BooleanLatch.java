package JavaMultiThread.ArtofConcurrencyProgramming.Chapter5;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * CountDownLatch的简化版，共享锁，阻塞的线程只需要一个信号signal就可以继续运行。
 *
 * @author xuanjian.xuwj
 */
public class BooleanLatch {
	private static class Sync extends AbstractQueuedSynchronizer {
		boolean isSignalled() { return getState() != 0; }

		@Override
		protected int tryAcquireShared(int ignore) {
			return isSignalled() ? 1 : -1;
		}

		@Override
		protected boolean tryReleaseShared(int ignore) {
			setState(1);
			return true;
		}
	}

	private final Sync sync = new Sync();
	public boolean isSignalled() { return sync.isSignalled(); }
	public void signal()         { sync.releaseShared(1); }
	public void await() throws InterruptedException {
		sync.acquireSharedInterruptibly(1);
	}
}
