package 数据结构.demo;

/**
 * 思路：使用两把锁，同时起两个线程，线程t1先获取锁lock1，睡眠2s，再获取锁lock2。线程2依次获取锁lock2和lock1。这样就可以模拟死锁。
 */
public class DeadLockSimulation {

    /** 两把锁 */
    private static Object lock1 = new Object();
    private static Object lock2 = new Object();

    /**
     * 模拟死锁
     */
    public static void deadLock() {
        Thread t1 = new Thread(() -> {
            synchronized (lock1) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (lock2){
                    System.out.println("t1");
                }
            }
        });
        Thread t2 = new Thread(() -> {
            synchronized (lock2) {
                synchronized (lock1){
                    System.out.println("t2");
                }
            }
        });
        t1.start();
        t2.start();
    }

    public static void main(String[] args) {
//        deadLock();


        System.out.println(tableSizeFor(4));
    }

    static final int MAXIMUM_CAPACITY = 1 << 30;

    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

}
