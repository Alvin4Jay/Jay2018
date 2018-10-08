package JDKSourceFileAnalysis.JUCLocks.ReentrantLock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 可重入的互斥锁(独占锁),测试
 *
 */
public class ReentrantLockTest1 {
    // 仓库
    private static class Depot {
        private int size;        // 仓库的实际数量
        private Lock lock;        // 独占锁

        public Depot() {
            this.size = 0;
            this.lock = new ReentrantLock(); //互斥锁
        }

        public void produce(int val) {
            lock.lock();
            try {
                size += val;
                System.out.printf("%s produce(%d) --> size=%d\n",
                        Thread.currentThread().getName(), val, size);
            } finally {
                lock.unlock();
            }
        }

        public void consume(int val) {
            lock.lock();
            try {
                size -= val;
                System.out.printf("%s consume(%d) <-- size=%d\n",
                        Thread.currentThread().getName(), val, size);
            } finally {
                lock.unlock();
            }
        }
    };

    // 生产者
    private static class Producer {
        private Depot depot;

        public Producer(Depot depot) {
            this.depot = depot;
        }

        // 消费产品：新建一个线程向仓库中生产产品。
        public void produce(final int val) {
            new Thread() {
                public void run() {
                    depot.produce(val);
                }
            }.start();
        }
    }

    // 消费者
    private static class Customer {
        private Depot depot;

        public Customer(Depot depot) {
            this.depot = depot;
        }

        // 消费产品：新建一个线程从仓库中消费产品。
        public void consume(final int val) {
            new Thread() {
                public void run() {
                    depot.consume(val);
                }
            }.start();
        }
    }

    public static void main(String[] args) {
        Depot mDepot = new Depot();
        Producer mPro = new Producer(mDepot);
        Customer mCus = new Customer(mDepot);

        mPro.produce(60);
        mPro.produce(120);
        mCus.consume(90);
        mCus.consume(150);
        mPro.produce(110);
    }
}
