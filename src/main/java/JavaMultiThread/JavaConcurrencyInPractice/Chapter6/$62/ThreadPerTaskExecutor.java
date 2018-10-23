package JavaMultiThread.JavaConcurrencyInPractice.Chapter6.$62;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

/**
 *  每个请求一个线程
 */
public class ThreadPerTaskExecutor implements Executor{
    @Override
    public void execute(@NotNull Runnable command) {
        new Thread(command).start();
    }
}
