package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$4.waittimeout;

/**
 * 等待超时模式
 *
 * @author xuanjian.xuwj
 */
public class WaitTimeout {

	/**
	 * 等待超时机制
	 * @param millis 最长等待时间
	 * @return 返回结果
	 * @throws InterruptedException 中断异常
	 *
	 */
	/* synchronized先获取对象锁 */
	public synchronized Object get(long millis) throws InterruptedException {
		// 超时时刻
		long future = System.currentTimeMillis() + millis;
		// 剩余等待时间
		long remaining = millis;
		// 最终结果
		Object result = null;

		// 循环条件判断，超时等待
		// 1.如果等待的结果已有，直接返回 2.等待的结果没有，先看remaining剩余等待时间是否大于0，否则直接返回,
		// 是则继续等待remaining时间
		while (result == null && remaining > 0) {
			// 等待remaining时间，释放锁
			wait(remaining);
			// 更新remaining剩余等待时间
			remaining = future - System.currentTimeMillis();
		}
		return result;
	}

}
