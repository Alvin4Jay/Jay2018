package JavaMultiThread.ArtofConcurrencyProgramming.Chapter8.CyclicBarrier;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 *  CyclicBarrier测试（一组线程相互等待）
 *  1.一组线程相互等待，等到这些线程都到达Barrier时，继续执行
 *  2.可以重用（继续执行时，CyclicBarrier被重置）
 */
public class CyclicBarrierTest {
	public static void main(String[] args) {
		int N = 4;
		CyclicBarrier barrier = new CyclicBarrier(N);
		for (int i = 0; i < N; i++) {
			new Writer(barrier).start();
		}
	}


	static class Writer extends Thread {
		private CyclicBarrier barrier;

		public Writer(CyclicBarrier barrier) {
			this.barrier = barrier;
		}

		@Override
		public void run() {
			System.out.println("线程" + Thread.currentThread().getName() + "正在写入数据...");

			try {
				Thread.sleep(5000);  //以睡眠来模拟写入数据操作
				System.out.println("线程" + Thread.currentThread().getName() + "写入数据完毕，等待其他线程写入完毕");
				barrier.await();  //等待其他线程执行完成
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}

			System.out.println("所有线程写入完毕，继续处理其他任务...");
		}
	}
}
