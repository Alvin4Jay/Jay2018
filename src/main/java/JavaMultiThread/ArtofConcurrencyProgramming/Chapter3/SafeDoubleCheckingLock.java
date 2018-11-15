package JavaMultiThread.ArtofConcurrencyProgramming.Chapter3;

/**
 * 双重检查锁定,线程安全 volatile
 *
 * @author xuanjian.xuwj
 */
public class SafeDoubleCheckingLock {
	private volatile static Instance instance;

	public static Instance getInstance() {
		if (instance == null) {
			synchronized (SafeDoubleCheckingLock.class) {
				if (instance == null) {
					instance = new Instance();
				}
			}
		}
		return instance;
	}

}
