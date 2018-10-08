package JDKSourceFileAnalysis.ThreadPriority;

/**
 * 线程优先级：在一个线程内创建的子线程，与父线程具有相同的优先级
 */
public class Demo02 {

    public static void main(String[] args) {


        System.out.println(Thread.currentThread().getName() + " thread priority: "
                + Thread.currentThread().getPriority());  //父线程


        new MyThread("child").start(); //子线程，优先级与父线程一致


    }

    static class MyThread extends Thread{
        public MyThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " thread priority: "
                    + Thread.currentThread().getPriority());
        }
    }
}
