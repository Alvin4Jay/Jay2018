package JavaMultiThread.ArtofConcurrencyProgramming.Chapter8.CountDownLatch;

import java.util.concurrent.CountDownLatch;

/**
 *  锁存器（共享锁）
 *  1.一组线程等待另一组线程执行任务完成之后再执行
 *  2.不可重用
 */
public class CountDownLatchTest {
	private static final int LATCH_SIZE = 5;  // 5个
	private static CountDownLatch doneSignal;  // 完成信号

    public static void main(String[] args) {
        try {
            doneSignal = new CountDownLatch(LATCH_SIZE);
			// 新建5个任务执行
            for (int i = 0; i < LATCH_SIZE; i++) {
                new InnerThread().start();
            }

            System.out.println("main thread wait");
			// 主线程main等待其余5个线程执行完成
			doneSignal.await();  // 等待1s

            System.out.println("main thread start");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static class InnerThread extends Thread{
        @Override
        public void run() {
            try {
				System.out.println(Thread.currentThread().getName() + " sleep 1000 ms");
                Thread.sleep(1000);

				// count - 1
				doneSignal.countDown(); // 减到0时，主线程开始继续执行

			} catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
