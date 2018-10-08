package JDKSourceFileAnalysis.JUCAtomic.BasicType;

import java.util.concurrent.atomic.AtomicLong;

/**
 *  AtomicLong测试
 */
public class AtomicLongTest {
    private static AtomicLong num = new AtomicLong(0);

    public static void sleep(int mills){
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while(i < 10){
                    sleep(100);
                    System.out.println(Thread.currentThread().getName() + "---" + num.incrementAndGet());
                    i ++;
                }
            }
        }, "t1");
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while(i < 10){
                    sleep(100);
                    System.out.println(Thread.currentThread().getName() + "---" + num.incrementAndGet());
                    i ++;
                }
            }
        }, "t2");

        t1.start();
        t2.start();
    }
}
