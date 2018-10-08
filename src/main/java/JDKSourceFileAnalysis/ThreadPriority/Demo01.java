package JDKSourceFileAnalysis.ThreadPriority;

/**
 *  线程优先级(优先级高的先执行)
 */
public class Demo01 {
    public static void main(String[] args) {

        Thread t1 = new MyThread("t1");
        Thread t2 = new MyThread("t2");

        t1.setPriority(1);  //设置优先级
        t2.setPriority(10);

        System.out.println(Thread.currentThread().getName() + " priority: "
                + Thread.currentThread().getPriority());

        t1.start();  //优先级高的先执行
        t2.start();  //优先级低的后执行

    }


    static class MyThread extends Thread{
        public MyThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread().getName() + " loop " + i);
            }
        }
    }
}
