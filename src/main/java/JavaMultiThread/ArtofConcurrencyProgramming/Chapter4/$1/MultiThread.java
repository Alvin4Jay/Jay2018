package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * 获取线程信息
 *
 * @author xuanjian.xuwj
 */
public class MultiThread {

	public static void main(String[] args) {
		// 获取Java线程管理MXBean
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

		// 获取线程信息
		ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);

		// 打印线程id和name
		for (ThreadInfo threadInfo : threadInfos) {
			System.out.println("[" + threadInfo.getThreadId() + "] " + threadInfo.getThreadName());
		}

	}

}
