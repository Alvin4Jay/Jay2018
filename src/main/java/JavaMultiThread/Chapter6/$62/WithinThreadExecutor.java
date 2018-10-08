package JavaMultiThread.Chapter6.$62;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

/**
 * 同步方式执行任务
 */
public class WithinThreadExecutor implements Executor{
    @Override
    public void execute(@NotNull Runnable command) {
        command.run();
    }
}
