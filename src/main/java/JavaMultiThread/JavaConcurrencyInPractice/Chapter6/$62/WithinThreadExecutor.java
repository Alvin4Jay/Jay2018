package JavaMultiThread.JavaConcurrencyInPractice.Chapter6.$62;

import java.util.concurrent.Executor;

/**
 * 同步方式执行任务
 */
public class WithinThreadExecutor implements Executor{
    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
