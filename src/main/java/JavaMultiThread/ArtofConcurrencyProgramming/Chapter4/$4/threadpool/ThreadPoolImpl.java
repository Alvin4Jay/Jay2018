package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$4.threadpool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ThreadPoolImpl 线程池实现
 *
 * @author xuanjian.xuwj
 */
public class ThreadPoolImpl<Task extends Runnable> implements ThreadPool<Task> {
	/** 线程池工作者最大数量 */
	private static final int MAX_WORKER_NUM = 10;
	/** 线程池工作者默认数量 */
	private static final int DEFAULT_WORKER_NUM = 5;
	/** 线程池工作者最小数量 */
	private static final int MIN_WORKER_NUM = 1;
	/** 任务列表 */
	private final LinkedList<Task> tasks = new LinkedList<>();
	/** 工作者列表 */
	private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());
	/** 目前的工作者数量 */
	private int workerCount;
	/** 工作者线程编号 */
	private AtomicLong workerNum = new AtomicLong(0);

	public ThreadPoolImpl() {
		workerCount = DEFAULT_WORKER_NUM;
		initializeWorkers(workerCount);
	}

	/**
	 * 指定线程池工作者数量
	 * @param num worker数量
	 */
	public ThreadPoolImpl(int num) {
		workerCount = num > MAX_WORKER_NUM ? MAX_WORKER_NUM : (num < MIN_WORKER_NUM ? MIN_WORKER_NUM : num);
		initializeWorkers(workerCount);
	}

	/**
	 * 执行任务
	 * @param task 任务，实现Runnable接口
	 */
	@Override
	public void execute(Task task) {
		if (task != null) {
			// wait/notify机制 生产者，只通知一个消费者(Worker)
			synchronized (tasks) {
				tasks.addLast(task);
				tasks.notify();
			}
		}
	}

	/**
	 * 关闭线程池
	 */
	@Override
	public void shutdown() {
		synchronized (tasks) {
			for (Worker worker : workers) {
				worker.shutdown();
			}
			workers.clear();
			workerCount = 0;
		}
	}

	/**
	 * 添加工作者
	 * @param num 新增的Worker数量
	 */
	@Override
	public void addWorkers(int num) {
		synchronized (tasks) {
			// 总工作者数不能超过最大值
			int add = 0;
			if (this.workerCount + num > MAX_WORKER_NUM) {
				add = MAX_WORKER_NUM - this.workerCount;
			}
			initializeWorkers(add);
			this.workerCount += add;
		}
	}

	/**
	 * 移除工作者
	 * @param num 移除的Worker数量
	 */
	@Override
	public void removeWorkers(int num) {
		synchronized (tasks) {
			// 参数检查
			if (num > this.workerCount) {
				throw new IllegalArgumentException("num > workerCount");
			}
			int count = 0;
			while (count < num) {
				Worker worker = workers.get(count);
				if (workers.remove(worker)) {
					worker.shutdown();
					count++;
				}
			}
			this.workerCount -= count;
		}
	}

	/**
	 * 获取等待执行的任务数量
	 * @return 等待执行的任务数量
	 */
	@Override
	public int getTaskSize() {
		return tasks.size();
	}

	/**
	 * 初始化工作者
	 * @param num
	 */
	private void initializeWorkers(int num) {
		for (int i = 0; i < num; i++) {
			Worker worker = new Worker();
			workers.add(worker);
			Thread thread = new Thread(worker, "Worker--" + workerNum.getAndIncrement());
			thread.start();
		}
	}

	/**
	 * 工作者线程，处理任务
	 */
	class Worker implements Runnable {
		/** 控制工作者线程是否工作 */
		private volatile boolean running = true;

		@Override
		public void run() {
			while (running) {
				Task task = null;
				// wait/notify机制 消费者
				synchronized (tasks) {
					// tasks无任务，等待
					while (tasks.isEmpty()) {
						try {
							tasks.wait();
						} catch (InterruptedException e) {
							// 若当前线程被中断，则保留中断标记，退出
							Thread.currentThread().interrupt();
							return;
						}
					}
					task = tasks.removeFirst();
				}
				if (task != null) {
					try {
						// 执行任务
						task.run();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		/**
		 * 关闭工作者
		 */
		public void shutdown() {
			this.running = false;
		}
	}

}
