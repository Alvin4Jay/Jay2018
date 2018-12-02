package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$4.threadpool;

/**
 * 线程池 接口
 *
 * @author xuanjian.xuwj
 */
public interface ThreadPool<Task extends Runnable> {

	/**
	 * 执行任务
	 * @param task 任务，实现Runnable接口
	 */
	void execute(Task task);

	/**
	 * 关闭线程池
 	 */
	void shutdown();

	/**
	 * 添加Worker
	 * @param num 新增的Worker数量
	 */
	void addWorkers(int num);

	/**
	 * 移除Worker
	 * @param num 移除的Worker数量
	 */
	void removeWorkers(int num);

	/**
	 * 获取等待执行的任务数量
	 * @return 等待执行的任务数量
	 */
	int getTaskSize();

}
