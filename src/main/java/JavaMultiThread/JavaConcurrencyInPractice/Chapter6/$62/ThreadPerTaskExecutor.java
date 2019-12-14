package JavaMultiThread.JavaConcurrencyInPractice.Chapter6.$62;

import java.util.concurrent.Executor;

/**
 *  每个请求一个线程
 */
public class ThreadPerTaskExecutor implements Executor{
    @Override
    public void execute( Runnable command) {
        new Thread(command).start();
    }
}
