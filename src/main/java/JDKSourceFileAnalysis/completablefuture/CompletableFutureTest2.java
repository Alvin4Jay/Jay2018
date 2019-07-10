package JDKSourceFileAnalysis.completablefuture;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * {@link CompletableFuture} 使用实例2
 *
 * @author xuanjian
 */
public class CompletableFutureTest2 {

    /**
     * 1.创建完成的CompletableFuture
     */
    private static void completedFutureExample() {
        CompletableFuture<String> future = CompletableFuture.completedFuture("message");
        assertTrue(future.isDone());
        assertEquals("message", future.getNow(null));
    }

    /**
     * 2.运行简单的异步场景
     * <p>
     * - CompletableFuture 是异步执行方式.
     * - 使用 ForkJoinPool 实现异步执行，这种方式使用了 daemon 线程执行 Runnable 任务.
     */
    private static void runAsyncExample() {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            assertTrue(Thread.currentThread().isDaemon()); // ForkJoin pool daemon线程
            randomSleep();
        });

        assertFalse(future.isDone());
        sleepEnough();
        assertTrue(future.isDone());
    }

    private static void sleepEnough() {
        try {
            TimeUnit.SECONDS.sleep(6);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void randomSleep() {
        Random random = new Random();
        try {
            TimeUnit.SECONDS.sleep(random.nextInt(5) + 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 3.同步执行动作示例
     */
    private static void thenApplyExample() {
        // 同步执行
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApply(str -> {
            assertFalse(Thread.currentThread().isDaemon()); // main线程
            return str.toUpperCase();
        });
        assertEquals("MESSAGE", cf.getNow(null));
    }

    /**
     * 4.异步执行动作示例
     */
    private static void thenApplyAsyncExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
            assertTrue(Thread.currentThread().isDaemon()); // ForkJoin pool daemon线程
            randomSleep();
            return s.toUpperCase();
        });
        assertNull(cf.getNow(null));
        assertEquals("MESSAGE", cf.join());
    }

    /**
     * 使用固定的线程池完成异步执行动作示例
     */
    private static ExecutorService executor = Executors.newFixedThreadPool(3, new ThreadFactory() {
        int count = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "custom-executor-" + count++);
        }
    });

    private static void thenApplyAsyncWithExecutorExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
            assertTrue(Thread.currentThread().getName().startsWith("custom-executor-"));
            assertFalse(Thread.currentThread().isDaemon());
            randomSleep();
            return s.toUpperCase();
        }, executor);
        assertNull(cf.getNow(null));
        assertEquals("MESSAGE", cf.join());
    }

    /**
     * 作为消费者消费计算结果示例
     */
    private static void thenAcceptExample() {
        StringBuilder sb = new StringBuilder();
        CompletableFuture.completedFuture("thenAccept message").thenAccept(sb::append);
        assertTrue("Result was empty", sb.length() > 0);
    }

    /**
     * 异步消费示例
     */
    private static void thenAcceptAsyncExample() {
        StringBuilder result = new StringBuilder();
        CompletableFuture<Void> cf = CompletableFuture.completedFuture("thenAcceptAsync message")
                .thenAcceptAsync(result::append);
        cf.join();
        assertTrue("Result was empty", result.length() > 0);
    }

    /**
     * 运行两个阶段后执行
     */
    private static void runAfterBothExample() {
        String original = "Message";
        StringBuilder result = new StringBuilder();
        CompletableFuture.completedFuture(original).thenApply(String::toUpperCase).runAfterBoth(
                CompletableFuture.completedFuture(original).thenApply(String::toLowerCase),
                () -> result.append("done"));
        assertTrue("Result was empty", result.length() > 0);
    }

    private static void thenAcceptBothExample() {
        String original = "Message";
        StringBuilder result = new StringBuilder();
        CompletableFuture.completedFuture(original).thenApply(String::toUpperCase).thenAcceptBoth(
                CompletableFuture.completedFuture(original).thenApply(String::toLowerCase),
                (s1, s2) -> result.append(s1).append(s2));
        assertEquals("MESSAGEmessage", result.toString());
    }

    public static void main(String[] args) throws Exception {

        // completedFutureExample();
        // runAsyncExample();
        // thenApplyExample();
        // thenApplyAsyncExample();
        // thenApplyAsyncWithExecutorExample();
        // thenAcceptExample();
        // thenAcceptAsyncExample();
        // runAfterBothExample();
        thenAcceptBothExample();

    }

}
