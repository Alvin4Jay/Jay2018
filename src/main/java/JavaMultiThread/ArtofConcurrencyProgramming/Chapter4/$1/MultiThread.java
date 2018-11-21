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

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

		ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);

		for (ThreadInfo threadInfo : threadInfos) {
			System.out.println("[" + threadInfo.getThreadId() + "] " + threadInfo.getThreadName());
		}

	}

}
