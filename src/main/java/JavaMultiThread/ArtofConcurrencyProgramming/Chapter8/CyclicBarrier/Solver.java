package JavaMultiThread.ArtofConcurrencyProgramming.Chapter8.CyclicBarrier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * CyclicBarrier示例 多线程计算，并将结果合并
 *
 * @author xuanjian.xuwj
 */
public class Solver {
	private final int N; // 行数
	private final float[][] data; // 二维数据
	private final CyclicBarrier barrier; // 同步屏障

	class Worker implements Runnable {
		int myRow; // 第几行

		Worker(int row) {
			myRow = row;
		}

		@Override
		public void run() {
			while (!done()) {
				processRow(myRow); // 处理这一行

				try {
					barrier.await(); // 处理完等待
				} catch (InterruptedException | BrokenBarrierException ex) {
					return;
				}
			}
		}
	}

	public Solver(float[][] matrix) throws InterruptedException {
		data = matrix;
		N = matrix.length;
		Runnable barrierAction = new Runnable() { // 所有线程处理完之后先执行的动作
			@Override
			public void run() {
				mergeRows(); // 合并结果，此时done()返回true
			}
		};
		barrier = new CyclicBarrier(N, barrierAction);

		List<Thread> threads = new ArrayList<Thread>(N);
		for (int i = 0; i < N; i++) {
			Thread thread = new Thread(new Worker(i));
			threads.add(thread);
			thread.start();
		}

		// wait until done
		for (Thread thread : threads) {
			thread.join(); // 等待所有线程处理完成
		}
	}

	// 合并结果
	private void mergeRows() {

	}

	// 处理一行
	private void processRow(int row) {

	}

	// 是否已完成
	private boolean done() {
		return false;
	}
}
