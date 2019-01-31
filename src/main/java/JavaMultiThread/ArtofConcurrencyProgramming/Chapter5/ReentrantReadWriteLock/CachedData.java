package JavaMultiThread.ArtofConcurrencyProgramming.Chapter5.ReentrantReadWriteLock;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 锁降级: 先获取写锁，再获取读锁，最后释放写锁
 *
 * @author xuanjian.xuwj
 */
public class CachedData {
	private Object data;
	private volatile boolean cacheValid;
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	public void processCachedData() {
		rwl.readLock().lock();
		if (!cacheValid) {
			rwl.readLock().unlock();
			rwl.writeLock().lock(); // 先获取写锁
			try {
				if (!cacheValid) {
					data = "new Data";
					cacheValid = true;
				}
				rwl.readLock().lock(); // 再获取读锁
			} finally {
				rwl.writeLock().unlock(); // 再释放写锁
			}
		}

		try {
			use(data); // 使用数据
		} finally {
			rwl.readLock().unlock();
		}
	}

	// 使用数据
	private void use(Object data) {
		System.out.println(data);
	}

}
