package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Java线程优先级测试
 *
 * @author xuanjian.xuwj
 */
public class Priority {

	private static volatile boolean notStart = true;

	private static volatile boolean notEnd = true;

	public static void main(String[] args) throws InterruptedException {

		List<Task> tasks = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			int priority = i < 5 ? Thread.MIN_PRIORITY : Thread.MAX_PRIORITY;
			Task task = new Task(priority);
			tasks.add(task);
			Thread thread = new Thread(task, "Thread: " + i);
			thread.setPriority(priority);
			thread.start();
		}

		// 10个线程开始计数
		notStart = false;
		// 主线程main Sleep 10s
		TimeUnit.SECONDS.sleep(10);
		// 10个线程计数结束
		notEnd = false;

		for (Task task : tasks) {
			System.out.println("Task Priority: " + task.priority + ", count: " + task.count);
		}

	}

	static class Task implements Runnable {
		private int priority;
		private int count;

		public Task(int priority) {
			this.priority = priority;
		}

		@Override
		public void run() {
			while (notStart) {
				// 当前线程让出CPU执行权，由RUNNING转为READY就绪状态，
				// 下次这个线程仍有可能执行
				Thread.yield();
			}
			while (notEnd) {
				Thread.yield();
				count++;
			}
		}
	}

}
