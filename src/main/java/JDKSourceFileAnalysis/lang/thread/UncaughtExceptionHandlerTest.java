package JDKSourceFileAnalysis.lang.thread;

/**
 * UncaughtExceptionHandler测试
 *
 * @author xuanjian
 */
public class UncaughtExceptionHandlerTest {

    public static void main(String[] args) {

        Thread t = new Thread(() -> {
            throw new IllegalStateException("test");
        });

        t.setUncaughtExceptionHandler((thread, e) -> System.out.println(e.getClass().getSimpleName() + "--" + e.getMessage()));

        t.start();
    }

}
