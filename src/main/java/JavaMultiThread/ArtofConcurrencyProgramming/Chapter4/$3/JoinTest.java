package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$3;

import JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1.SleepUtils;

/**
 * Thread.Join() Test
 *
 * @author xuanjian.xuwj
 */
public class JoinTest {
	public static void main(String[] args){
		Thread previous = Thread.currentThread();
		for (int i = 0; i < 10; i++) {
			// 每个线程拥有前一个线程的引用，只有前一个线程终止执行，这个线程才能从等待中返回，继续执行
			Thread current = new Thread(new Domino(previous), String.valueOf(i));
			current.start();
			previous = current;
		}
		SleepUtils.second(5);
		System.out.println(Thread.currentThread().getName() + " terminate.");
	}

	private static class Domino implements Runnable{
		private Thread previous;

		public Domino(Thread previous) {
			this.previous = previous;
		}

		@Override
		public void run() {
			try {
				previous.join();
				System.out.println(Thread.currentThread().getName() + " terminate.");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
