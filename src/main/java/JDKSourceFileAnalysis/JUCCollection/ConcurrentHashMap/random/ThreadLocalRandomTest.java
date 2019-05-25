package JDKSourceFileAnalysis.JUCCollection.ConcurrentHashMap.random;

import java.util.concurrent.ThreadLocalRandom;

/**
 * {@link java.util.concurrent.ThreadLocalRandom}
 *
 * @author <a href=mailto:xuweijay@gmail.com>xuanjian</a>
 */
public class ThreadLocalRandomTest {
    public static void main(String[] args) {

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < 10; i++) {
            System.out.println(random.nextInt(5));
        }

    }
}
