package JavaMultiThread.ArtofConcurrencyProgramming.Chapter8.CountDownLatch;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * CountDownLatch的简化版，共享锁，阻塞的线程(多个)只需要一个信号signal就可以继续运行。
 *
 * @author xuanjian.xuwj
 */
public class BooleanLatch {
	private static class Sync extends AbstractQueuedSynchronizer {
		boolean isSignalled() {
			return getState() != 0; // 初始state=0，为1时表示等待线程可以获取共享锁
		}

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

	public boolean isSignalled() {
		return sync.isSignalled();
	}

	public void signal() {
		sync.releaseShared(1);
	}

	public void await() throws InterruptedException {
		sync.acquireSharedInterruptibly(1);
	}
}
