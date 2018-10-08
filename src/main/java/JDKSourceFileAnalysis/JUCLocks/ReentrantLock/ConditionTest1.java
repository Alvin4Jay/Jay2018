package JDKSourceFileAnalysis.JUCLocks.ReentrantLock;

/**
 *  Condition测试-----使用Synchronized(Object)
 */
public class ConditionTest1 {
    public static void main(String[] args) {

        ThreadA ta = new ThreadA("ta");

        synchronized(ta) { // 通过synchronized(ta)获取“对象ta的同步锁”
            try {
                System.out.println(Thread.currentThread().getName()+" start ta");
                ta.start(); //main线程运行，ta线程阻塞

                System.out.println(Thread.currentThread().getName()+" block");
                ta.wait();    // main阻塞，ta运行
                //main运行
                System.out.println(Thread.currentThread().getName()+" continue");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class ThreadA extends Thread{

        public ThreadA(String name) {
            super(name);
        }

        public void run() {
            synchronized (this) { // 通过synchronized(this)获取“当前对象的同步锁”
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName()+" wakeup others");
                this.notifyAll();    // 唤醒“当前对象上的等待线程”
            }
        }
    }
}
