package JavaMultiThread.ArtofConcurrencyProgramming.Chapter5;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock
 *
 * @author xuanjian.xuwj
 */
public class ReentrantLockDemo {
	public static void main(String[] args){
		Lock lock = new ReentrantLock();
		lock.lock();
		try {
			System.out.println(lock);
		} finally {
			lock.unlock();
		}

	}
}
