package 数据结构.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * 思路：使用信号量控制。N个线程对应N个信号量(每个信号量许可数是1)，分别控制每个线程是否能获取到信号量，能则打印当前的数字number。
 * 并且由当前的线程在打印完数字之后，释放下一个线程对应的信号量，于是当前线程在下一个循环获取信号量时阻塞，下一个线程就可以获取到对应的
 * 信号量许可。退出条件是：递增的数字 >=100。
 */
public class MultiThreadSequencePrint {

    /** 递增数字 */
    private static int number = 0;

    /**
     * 多线程顺序打印1-100的数字
     * @param n 线程数
     */
    public static void multiThreadSequencePrint(int n) {
        if (n <= 0) {
            return;
        }

        List<Semaphore> controllers = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            controllers.add(i == 0? new Semaphore(1) : new Semaphore(0));
        }

        for (int i = 0; i < n; i++) {
            Semaphore curController = controllers.get(i);
            Semaphore nextController = controllers.get(i + 1 == n ? 0 : i + 1);
            int index = i;
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        curController.acquire();
                        System.out.println("thread" + index + ": " + number);
                        if (number >= 100) {
                            System.exit(0);
                        }
                        number++;
                        nextController.release();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.start();
        }
    }

    public static void main(String[] args) {
        multiThreadSequencePrint(3);
    }

}
