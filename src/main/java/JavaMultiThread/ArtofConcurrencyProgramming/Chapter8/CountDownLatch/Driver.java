package JavaMultiThread.ArtofConcurrencyProgramming.Chapter8.CountDownLatch;

import java.util.concurrent.CountDownLatch;

/**
 * CountDownLatch示例   startSignal启动信号，doneSignal任务完成信号
 *
 * @author xuanjian.xuwj
 */
public class Driver {

	private static final int WORKER_COUNT = 10;

	void main() throws InterruptedException {
		CountDownLatch startSignal = new CountDownLatch(1);
		CountDownLatch doneSignal = new CountDownLatch(WORKER_COUNT);

		// 创建、启动线程
		for (int i = 0; i < WORKER_COUNT; i++) {
			new Thread(new Worker(startSignal, doneSignal)).start();
		}

		// 先不让workers运行
		doSomethingElse();
		// 让所有的worker运行
		startSignal.countDown();
		doSomethingElse();
		// 等待所有worker执行完成
		doneSignal.await();
	}

	private void doSomethingElse() {

	}

	private static class Worker implements Runnable {
		private final CountDownLatch startSignal;
		private final CountDownLatch doneSignal;

		public Worker(CountDownLatch startSignal, CountDownLatch doneSignal) {
			this.startSignal = startSignal;
			this.doneSignal = doneSignal;
		}

		@Override
		public void run() {
			try {
				startSignal.await();
				doWork();
				// 执行完成
				doneSignal.countDown();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		private void doWork() {

		}
	}
}
