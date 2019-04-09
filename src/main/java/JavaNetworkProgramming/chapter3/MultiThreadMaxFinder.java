package JavaNetworkProgramming.chapter3;

import java.util.concurrent.*;

/**
 * @author xuanjian
 */
public class MultiThreadMaxFinder {
	public static int max(int[] data) throws ExecutionException, InterruptedException {
		if (data.length == 1) {
			return data[0];
		} else if (data.length == 0) {
			throw new IllegalArgumentException();
		}

		// 将任务分解为两部分
		FindMaxTask t1 = new FindMaxTask(data, 0, data.length / 2);
		FindMaxTask t2 = new FindMaxTask(data, data.length / 2, data.length);

		// 创建2个线程
		ExecutorService service = Executors.newFixedThreadPool(2);

		Future<Integer> f1 = service.submit(t1);
		Future<Integer> f2 = service.submit(t2);

		return Math.max(f1.get(), f2.get());

	}
}
