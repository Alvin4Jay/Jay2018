package JavaMultiThread.Chapter7.$71;

/**
 * 线程阻塞的时候调用interrupt()
 * interrupt()作用
 * 1. 中断阻塞线程，产生异常，退出阻塞状态
 * 2. 设置标记
 */
public class ThreadInterruptTest1 {

    public static void main(String[] args) {
        final Thread t = new Thread(new ThreadA());
        t.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                t.interrupt(); //t产生异常退出
            }
        }).start();

    }


    static class ThreadA implements Runnable{

        @Override
        public void run() {
            try {
                while(true){
                    Thread.sleep(1000); //阻塞的线程，其他线程代用该线程的interrupt()方法，会产生异常
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Thread Exit");
            }

        }
    }
}
