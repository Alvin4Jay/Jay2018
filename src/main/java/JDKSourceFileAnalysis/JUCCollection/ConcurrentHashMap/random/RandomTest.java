package JDKSourceFileAnalysis.JUCCollection.ConcurrentHashMap.random;

import java.util.Random;

/**
 * {@link java.util.Random} 测试
 *
 * @author <a href=mailto:xuweijay@gmail.com>xuanjian</a>
 */
public class RandomTest {
    public static void main(String[] args) {

        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            System.out.println(random.nextInt(5));
        }

    }
}
