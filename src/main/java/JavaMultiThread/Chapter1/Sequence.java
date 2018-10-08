package JavaMultiThread.Chapter1;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.Locale;

/**
 *  安全性问题2
 */
@ThreadSafe
public class Sequence {

    @GuardedBy("this") private int value;


    public synchronized int getNext(){
        return ++value;
    }

    public static void main(String[] args) {
        Sequence s  =new Sequence();
        MyThread t = new MyThread(s);
        Thread  thread1 = new Thread(t, "thread1");
        Thread  thread2 = new Thread(t, "thread2");
        thread1.start();
        thread2.start();
    }
}

class MyThread implements Runnable{
    private Sequence sequence;

    public MyThread(Sequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public void run() {
        for (int i = 0; i < 20; i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(String.format(Locale.CHINA, Thread.currentThread().getName()+"---Number:%d--Value:%d", i, sequence.getNext()));
        }
    }
}
