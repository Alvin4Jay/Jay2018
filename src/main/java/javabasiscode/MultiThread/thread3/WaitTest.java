package javabasiscode.MultiThread.thread3;

/**
 *  wait() 与 notify() 共同测试

 结果:
 main start t1
 main wait()
 t1 call notify()
 main continue

 */
public class WaitTest {
    public static void main(String[] args) {

        ThreadA t1 = new ThreadA("t1");

        synchronized (t1){

            try {
                //启动线程 "t1"
                System.out.println(Thread.currentThread().getName()+" start t1"); // main start t1
                t1.start();  //main线程得到了锁，t1等待

                //主线程等待t1通过notify()唤醒。
                System.out.println(Thread.currentThread().getName()+" wait()"); // main wait()
                t1.wait();  //使得main线程(当前线程等待) main释放锁，等待，t1获得锁，执行

                //通过t1线程调用t1对象的notify()方法获取锁
                System.out.println(Thread.currentThread().getName()+" continue"); // main continue

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }
}

class ThreadA extends Thread{

    public ThreadA(String name) {
        super(name);
    }

    @Override
    public void run() {
        synchronized (this){
            //t1 call notify()
            System.out.println(Thread.currentThread().getName() + " call notify()");

            this.notify(); // 唤醒当前的wait线程
        }
    }
}
