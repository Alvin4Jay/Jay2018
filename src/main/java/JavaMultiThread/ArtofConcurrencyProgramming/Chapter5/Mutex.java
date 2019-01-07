package JavaMultiThread.ArtofConcurrencyProgramming.Chapter5;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 自定义一个互斥锁(独占锁)
 */
public class Mutex implements Lock, java.io.Serializable {
	/**
	 * 内部类，自定义同步器
	 */
	private static class Sync extends AbstractQueuedSynchronizer {
		/**
		 * 是否处于占用状态
		 */
		@Override
		protected boolean isHeldExclusively() {
			return getState() == 1;
		}

		/**
		 * 当状态为0的时候获取锁
		 */
		@Override
		public boolean tryAcquire(int acquires) {
			// Otherwise unused
			assert acquires == 1;
			if (compareAndSetState(0, 1)) {
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
			return false;
		}

		/**
		 * 释放锁，将状态设置为0。只有在完全释放锁的时候，才会返回true。
		 * @param releases 释放的数量
		 * @return 成功true
		 */
		@Override
		protected boolean tryRelease(int releases) {
			// Otherwise unused
			assert releases == 1;
			if (getState() == 0) {
				throw new IllegalMonitorStateException();
			}
			setExclusiveOwnerThread(null);
			setState(0);
			return true;
		}

		/**
		 * 返回一个Condition，每个condition都包含了一个condition队列
		 * @return {@link Condition}
		 */
		Condition newCondition() {
			return new ConditionObject();
		}
	}

	/**
	 * 仅需要将操作代理到Sync上即可
	 */
	private final Sync sync = new Sync();

	@Override
	public void lock() {
		sync.acquire(1);
	}

	@Override
	public boolean tryLock() {
		return sync.tryAcquire(1);
	}

	@Override
	public void unlock() {
		sync.release(1);
	}

	@Override
	public Condition newCondition() {
		return sync.newCondition();
	}

	public boolean isLocked() {
		return sync.isHeldExclusively();
	}

	public boolean hasQueuedThreads() {
		return sync.hasQueuedThreads();
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		sync.acquireInterruptibly(1);
	}

	@Override
	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
		return sync.tryAcquireNanos(1, unit.toNanos(timeout));
	}
}