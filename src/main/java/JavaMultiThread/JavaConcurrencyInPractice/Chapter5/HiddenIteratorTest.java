package JavaMultiThread.JavaConcurrencyInPractice.Chapter5;

/**
 * Created by Jay on 2017/5/28.
 */
public class HiddenIteratorTest {
    public static void main(String[] args) throws InterruptedException {
        HiddenIterator h = new HiddenIterator();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                h.addTenThings();
            }
        }, "t1");

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                h.addTenThings();
            }
        }, "t2");
        t1.start();

        Thread.sleep(5);

        t2.start();
    }
}
