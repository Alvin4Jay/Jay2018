package JavaMultiThread.ArtofConcurrencyProgramming.Chapter3;

/**
 * 非线程安全的延迟初始化
 *
 * @author xuanjian.xuwj
 */
public class UnsafeLazyInitialization {

	private static Instance instance;

	public static Instance getInstance() {
		// 这里instance可能不为null, 但instance还未初始化
		if (instance == null) {
			instance = new Instance();
		}
		return instance;
	}

}
