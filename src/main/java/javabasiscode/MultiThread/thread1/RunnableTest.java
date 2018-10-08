package javabasiscode.MultiThread.thread1;

/**
 * 多线程 Runnable
 */
public class RunnableTest {
    public static void main(String[] args) {
        MyThread2 t = new MyThread2();

        Thread t1 = new Thread(t);  //t1
        Thread t2 = new Thread(t);  //t2
        Thread t3 = new Thread(t);  //t3
        t1.start();
        t2.start();
        t3.start();
    }
}

class MyThread2 implements Runnable{
    private volatile int ticket = 500;  //启动3个线程t1,t2,t3(它们共用一个Runnable对象)，这3个线程一共卖10张票！

    @Override
    public synchronized void run() {

        for (int i = 0; i < 200; i++) {
            if(this.ticket > 0){
                System.out.println(Thread.currentThread().getName() + "卖票:ticket"+ this.ticket--);
            }
        }


    }
}
