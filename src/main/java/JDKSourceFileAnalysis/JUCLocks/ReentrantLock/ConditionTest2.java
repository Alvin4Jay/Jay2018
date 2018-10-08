package JDKSourceFileAnalysis.JUCLocks.ReentrantLock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  Condition测试
 */
public class ConditionTest2 {
    private static Lock lock = new ReentrantLock(); //互斥锁
    private static Condition condition = lock.newCondition(); //条件(在该锁当前条件下的线程管理)

    public static void main(String[] args) {

        ThreadA ta = new ThreadA("ta");

        lock.lock(); // 获取锁
        try {
            System.out.println(Thread.currentThread().getName()+" start ta");
            ta.start(); //main运行,ta阻塞

            System.out.println(Thread.currentThread().getName()+" block");
            condition.await();    // main阻塞，ta获取锁运行

            System.out.println(Thread.currentThread().getName()+" continue");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();    // 释放锁
        }
    }

    static class ThreadA extends Thread{

        public ThreadA(String name) {
            super(name);
        }

        public void run() {
            lock.lock();    // 获取锁
            try {
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName()+" wakeup others");
                condition.signal();    // 唤醒“condition所在锁上的其它线程”
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();    // 释放锁
            }
        }
    }
}
