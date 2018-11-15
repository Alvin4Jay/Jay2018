package JavaMultiThread.ArtofConcurrencyProgramming.Chapter3;

/**
 * 使用synchronized实现线程安全的延迟初始化
 *
 * @author xuanjian.xuwj
 */
public class SafeLazyInitialization {
	private static Instance instance;

	public synchronized static Instance getInstance() {
		if (instance == null) {
			instance = new Instance();
		}
		return instance;
	}

}
