package JavaMultiThread.ArtofConcurrencyProgramming.Chapter10.ThreadPoolExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池实例
 */
public class ThreadPoolTest1 {
    public static void main(String[] args) {
		ExecutorService pool = Executors.newFixedThreadPool(2); //创建线程池 固定大小
        for (int i = 0; i < 5; i++) {
            pool.submit(new MyThread()); //提交任务
        }
        pool.shutdown(); //关闭线程池

    }

    static class MyThread implements Runnable{
        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " is running.");
        }
    }
}
