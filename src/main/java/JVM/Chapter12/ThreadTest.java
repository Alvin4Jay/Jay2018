package JVM.Chapter12;

/**
 * Created by Jay on 2017/6/16.
 */
public class ThreadTest {

    public static void main(String[] args) {
        Thread t = new MyThread("test");

        System.out.println(t.getName() + " status---" + t.getState());

        t.start();
    }

    static class MyThread extends Thread{
        public MyThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            System.out.println(this.getName() + " status---" + this.getState());
        }
    }
}
