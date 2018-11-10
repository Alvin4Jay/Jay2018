package JavaMultiThread.ArtofConcurrencyProgramming.Chapter3;

/**
 * volatile 特性示例
 *
 * @author xuanjian.xuwj
 */
public class VolatileFeaturesExample {

	private volatile long v1 = 0L;

	public void set(long l) {
		v1 = l;
	}

	public void getAndIncrment() {
		// 非原子性操作
		v1++;
	}

	public long get() {
		return v1;
	}

}
