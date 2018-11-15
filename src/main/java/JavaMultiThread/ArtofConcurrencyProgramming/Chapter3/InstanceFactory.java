package JavaMultiThread.ArtofConcurrencyProgramming.Chapter3;

/**
 * 基于类初始化的线程安全的延迟初始化方案
 *
 * @author xuanjian.xuwj
 */
public class InstanceFactory {
	// 静态类
	private static class InstanceHolder {
		public static Instance instance = new Instance();
	}

	public static Instance getInstance() {
		// 初始化 InstanceHolder
		return InstanceHolder.instance;
	}

}
