package JavaMultiThread.Chapter8;

import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

/**
 * Created by Jay on 2017/6/21.
 */
public class BoundedExecutor {
    private final Executor executor;
    private final Semaphore semaphore;

    public BoundedExecutor(Executor executor, int bound) {
        this.executor = executor;
        this.semaphore = new Semaphore(bound);
    }

    public void submitTask(final Runnable command) throws InterruptedException {
        semaphore.acquire();

        try {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        command.run();
                    } finally {
                        semaphore.release();
                    }
                }
            });
        } catch (Exception e) {
            semaphore.release();
        }

    }
}
