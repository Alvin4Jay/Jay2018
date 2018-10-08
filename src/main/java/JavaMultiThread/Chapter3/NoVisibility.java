package JavaMultiThread.Chapter3;

/**
 *  在没有同步的情况下共享变量
 */
public class NoVisibility {
    private static boolean ready;
    private static int number;

    private static class ReaderThread extends Thread{
        @Override
        public void run() {
            while(!ready){
                Thread.yield();  //线程让步
            }
            System.out.println(number);
        }
    }

    public static void main(String[] args) {
        new ReaderThread().start();

        number = 42;

        ready = true;


    }

}
