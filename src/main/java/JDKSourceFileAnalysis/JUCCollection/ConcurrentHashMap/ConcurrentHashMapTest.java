package JDKSourceFileAnalysis.JUCCollection.ConcurrentHashMap;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.*;

/**
 * ConcurrentHashMap Test
 *
 * @author xuanjian
 */
public class ConcurrentHashMapTest {

    private static final ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2,
            Long.MAX_VALUE, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
        private AtomicLong counter = new AtomicLong(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "thread-" + counter.incrementAndGet());
        }
    });

    public static void main(String[] args) {
        executor.execute(() -> {
            for (int i = 0; i < 1000; i++) {
                map.putIfAbsent(String.valueOf(i), String.valueOf(2 * i));
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        executor.execute(() -> {
            for (int i = 0; i < 1000; i++) {
                map.putIfAbsent(String.valueOf(i), String.valueOf(4 * i));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
