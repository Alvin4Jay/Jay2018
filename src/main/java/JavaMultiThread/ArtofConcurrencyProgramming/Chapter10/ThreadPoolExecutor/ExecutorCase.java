package JavaMultiThread.ArtofConcurrencyProgramming.Chapter10.ThreadPoolExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Executors工厂方法测试
 */
public class ExecutorCase {

    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {

        for (int i = 0; i < 20; i++) {
            executor.execute(new Task());
        }

        executor.shutdown();

    }

    static class Task implements Runnable{
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName());
        }
    }
}
