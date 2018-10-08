package JDKSourceFileAnalysis.ThreadPriority;

/**
 *  守护线程
 *  用户线程执行完成后，JVM和守护线程退出
 */
public class DaemonTest {

    public static void main(String[] args) {

        Thread userThread = new MyThread("UserThread");
        Thread daemonThread = new MyDaemon("DaemonThread");

        daemonThread.setDaemon(true);

        userThread.start();
        daemonThread.start(); //用户线程执行完成后，JVM和守护线程退出

    }


    static class MyThread extends Thread{
        public MyThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                System.out.println(this.getName() + " (isDaemon=" +
                        this.isDaemon() + ")" + " loop " + i);
            }
        }
    }

    static class MyDaemon extends Thread{
        public MyDaemon(String name) {
            super(name);
        }
        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                System.out.println(this.getName() + " (isDaemon=" +
                        this.isDaemon() + ")" + " loop " + i);
            }
        }
    }

}
