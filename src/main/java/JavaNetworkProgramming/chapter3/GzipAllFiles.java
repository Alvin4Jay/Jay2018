package JavaNetworkProgramming.chapter3;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 压缩文件的用户类
 *
 * @author xuanjian
 */
public class GzipAllFiles {
	private static final int THREAD_COUNT = 4;

	public static void main(String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

		for (String filename : args) {
			File f = new File(filename);
			if (f.exists()) {
				if (f.isDirectory()) {
					File[] files = f.listFiles();
					for (int i = 0; i < files.length; i++) {
						// 不递归处理目录
						if (!files[i].isDirectory()) {
							GzipRunnable runnable = new GzipRunnable(files[i]);
							executorService.submit(runnable);
						}
					}
				} else {
					GzipRunnable runnable = new GzipRunnable(f);
					executorService.submit(runnable);
				}
			}
		}

		executorService.shutdown();
	}
}
