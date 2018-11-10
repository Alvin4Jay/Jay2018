package JavaMultiThread.ArtofConcurrencyProgramming.Chapter3;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock Test.
 *
 * @author xuanjian.xuwj
 */
public class ReentrantLockExample {
	private int a = 0;
	private ReentrantLock lock = new ReentrantLock();

	public void write() {
		// 获取锁
		lock.lock();
		try {
			a++;
		} finally {
			// 释放锁
			lock.unlock();
		}
	}

	public void read() {
		// 获取锁
		lock.lock();
		try {
			int i = a;
			System.out.println(i);
		} finally {
			// 释放锁
			lock.unlock();
		}
	}

}
