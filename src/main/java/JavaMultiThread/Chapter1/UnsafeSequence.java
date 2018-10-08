package JavaMultiThread.Chapter1;

import net.jcip.annotations.NotThreadSafe;

import java.util.Locale;

/**
 *  非线程安全的数值序列产生器
 */
@NotThreadSafe
public class UnsafeSequence {
    private int value;

    //返回唯一的数值
    public int getNext(){
        return ++value;
    }

    public static void main(String[] args) {
        UnsafeSequence s  =new UnsafeSequence();
        MyThread2 t = new MyThread2(s);
        Thread  thread1 = new Thread(t);
        Thread  thread2 = new Thread(t);
        thread1.start();
        thread2.start();
    }
}

class MyThread2 implements Runnable{
    private UnsafeSequence sequence;

    public MyThread2(UnsafeSequence sequence) {
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
