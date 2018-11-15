package JavaMultiThread.ArtofConcurrencyProgramming.Chapter3;

/**
 * 双重检查锁定,非线程安全
 *
 * @author xuanjian.xuwj
 */
public class DoubleCheckingLock {
	private static Instance instance;

	public static Instance getInstance() {
		if (instance == null) {
			synchronized (DoubleCheckingLock.class) {
				if (instance == null) {
					instance = new Instance();
				}
			}
		}
		return instance;
	}

}
