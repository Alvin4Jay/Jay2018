package JavaMultiThread.Chapter8.CountDownLatch;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * CountDownLatch 示例， 任务分解 1-->N
 *
 * @author xuanjian.xuwj
 */
public class Driver2 {

	void main() throws InterruptedException {
		CountDownLatch doneSignal = new CountDownLatch(10); // 任务完成信号
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));

		// 创建、执行任务
		for (int i = 0; i < 10; i++) {
			executor.execute(new WorkerRunnable(doneSignal, i));
		}

		// 等待所有任务执行完成
		doneSignal.await();
	}

	private static class WorkerRunnable implements Runnable {
		private final CountDownLatch doneSignal;
		private final int i;

		public WorkerRunnable(CountDownLatch doneSignal, int i) {
			this.doneSignal = doneSignal;
			this.i = i;
		}

		@Override
		public void run() {
			try {
				doWork(i);
				// 执行完成
				doneSignal.countDown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private void doWork(int i) {

		}
	}
}
