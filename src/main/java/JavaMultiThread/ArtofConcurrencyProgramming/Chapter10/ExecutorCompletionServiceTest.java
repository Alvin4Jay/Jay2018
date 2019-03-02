package JavaMultiThread.ArtofConcurrencyProgramming.Chapter10;

import java.util.concurrent.*;

/**
 * ExecutorCompletionService测试
 *
 * @author xuanjian.xuwj
 */
public class ExecutorCompletionServiceTest {

	private static final int TASKS_NUM = 10;

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		Executor executor = Executors.newFixedThreadPool(2);

		ExecutorCompletionService<Integer> ecs = new ExecutorCompletionService<>(executor);

		for (int i = 1; i <= TASKS_NUM; i++) {
			ecs.submit(new Task(i), i);
		}

		for (int i = 0; i < TASKS_NUM; i++) {
			Integer res = ecs.take().get();
			System.out.println(res);
		}

	}

	private static class Task implements Runnable {
		private int sleepTime;

		Task(int sleepTime) {
			this.sleepTime = sleepTime;
		}

		@Override
		public void run() {
			try {
				TimeUnit.SECONDS.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
