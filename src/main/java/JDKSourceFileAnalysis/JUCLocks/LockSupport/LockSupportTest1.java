package JDKSourceFileAnalysis.JUCLocks.LockSupport;

/**
 * LockSupport 测试 wait notify
 */
public class LockSupportTest1 {
    public static void main(String[] args) {

        ThreadA ta = new ThreadA("ta");

        synchronized(ta) { // 通过synchronized(ta)获取“对象ta的同步锁”
            try {
                System.out.println(Thread.currentThread().getName()+" start ta");
				ta.start(); // 主线程运行，ta阻塞

                System.out.println(Thread.currentThread().getName()+" block");
                // 主线程等待，ta运行
                ta.wait();

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

		@Override
		public void run() {
            synchronized (this) { // 通过synchronized(this)获取“当前对象的同步锁”
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName()+" wakeup others");
                notify();    // 唤醒“当前对象上的等待线程”
            }
        }
    }
}
