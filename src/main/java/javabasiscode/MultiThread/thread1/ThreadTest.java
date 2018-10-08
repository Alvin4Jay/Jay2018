package javabasiscode.MultiThread.thread1;

/**
 * Thread 多线程
 */
public class ThreadTest {
    public static void main(String[] args) {
        MyThread t1 = new MyThread();
        MyThread t2 = new MyThread();
        MyThread t3 = new MyThread();

        t1.start();
        t2.start();
        t3.start();
    }
}

class MyThread extends Thread{
    private int ticket = 10;  //每个线程各自拥有10张票

    @Override
    public void run() {

        for (int i = 0; i < 20; i++) {
            if(this.ticket > 0){
                System.out.println(this.getName() + " 卖票: ticket" + this.ticket--);
            }
        }


    }
}