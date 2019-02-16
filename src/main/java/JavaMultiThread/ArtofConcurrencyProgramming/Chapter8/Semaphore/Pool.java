package JavaMultiThread.ArtofConcurrencyProgramming.Chapter8.Semaphore;

import java.util.concurrent.Semaphore;

/**
 * Semaphore示例 有限资源池的并发获取控制
 *
 * @author xuanjian.xuwj
 */
public class Pool {
	private static final int MAX_AVAILABLE = 100;
	/** 信号量，资源数100，公平机制 */
	private final Semaphore available = new Semaphore(MAX_AVAILABLE, true);

	/** 获取item */
	public Object getItem() throws InterruptedException {
		// 先获取信号量的一个许可
		available.acquire();
		return getNextAvailableItem();
	}

	/** 放回item */
	public void putItem(Object x) {
		if (markAsUnused(x)) {
			// 资源释放后，释放信号量的一个许可
			available.release();
		}
	}

	/** 资源与使用情况 */
	private Object[] items = new Object[MAX_AVAILABLE];
	private boolean[] used = new boolean[MAX_AVAILABLE];

	/**
	 * 获取下一个可用资源
	 */
	private synchronized Object getNextAvailableItem() {
		for (int i = 0; i < items.length; i++) {
			if (!used[i]) {
				used[i] = true;
				return items[i];
			}
		}
		return null;
	}

	/**
	 * 标记该资源为未使用状态
	 */
	private synchronized boolean markAsUnused(Object x) {
		for (int i = 0; i < items.length; i++) {
			if (items[i] == x) {
				if (used[i]) {
					used[i] = false;
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}
}
