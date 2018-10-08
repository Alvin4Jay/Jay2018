package javabasiscode.MultiThread.thread2;

/**
 *  当一个线程访问“某对象”的“synchronized方法”或者“synchronized代码块”时,
 *  其他线程对“该对象”的该“synchronized方法”或者“synchronized代码块”的
 *  访问将被阻塞。
 */
public class Demo1_1 {
    public static void main(String[] args) {
        Runnable demo = new MyRunnable(); // 新建“Runnable对象”

        new Thread(demo, "t1").start(); // 新建“线程t1”, t1是基于demo这个Runnable对象
        new Thread(demo, "t2").start(); // 新建“线程t2”, t2是基于demo这个Runnable对象


    }
}

class MyRunnable implements Runnable{

    @Override
    public void run() {
        synchronized (this){  //对象锁

            try {
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(100); //当前线程休眠100ms

                    System.out.println(Thread.currentThread().getName() + " loop " + i);

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }
}
