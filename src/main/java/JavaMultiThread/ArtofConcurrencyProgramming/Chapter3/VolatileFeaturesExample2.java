package JavaMultiThread.ArtofConcurrencyProgramming.Chapter3;

/**
 * volatile 特性示例
 *
 * @author xuanjian.xuwj
 */
public class VolatileFeaturesExample2 {

	private long v1 = 0L;

	// 同步
	public synchronized void set(long l) {
		v1 = l;
	}

	// 非同步方法
	public void getAndIncrment() {
		// 非原子性操作
		long temp = get();
		temp += 1;
		set(temp);
	}

	// 同步
	public synchronized long get() {
		return v1;
	}

}
