package JavaMultiThread.JavaConcurrencyInPractice.Chapter7.$71;

/**
 * Created by Jay on 2017/6/12.
 */
public class ThreadInterruptTest2 {

    public static void main(String[] args) {

        ThreadA t = new ThreadA();
        t.start();

        try {
            Thread.sleep(1000);

            t.interrupt(); //interrupt()并不会终止处于“运行状态”的线程！它只会将线程的中断标记设为true。

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    static class ThreadA extends Thread{
        @Override
        public void run() {
            while(!isInterrupted()){
                System.out.println(this.isInterrupted()); //中断标记false
            }
            System.out.println(this.isInterrupted()); //true
            System.out.println("Thread exit...");
        }
    }
}
