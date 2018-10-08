package javabasiscode.MultiThread.thread1;

/**
 *  run() 与 start()区别
 */
public class Demo {
    public static void main(String[] args) {

        Thread mythread=new MyThread3("mythread");

        System.out.println(Thread.currentThread().getName() + " is calling mythread.run()");
        mythread.run();

        System.out.println(Thread.currentThread().getName() + " is calling mythread.start()");
        mythread.start();
    }
}

class MyThread3 extends Thread{
    public MyThread3(String name) {
        super(name);
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " is calling run()...");
    }
}
