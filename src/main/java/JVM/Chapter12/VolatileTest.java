package JVM.Chapter12;

/**
 *  volatile变量的运算
 *
 *  VM参数：-XX:+UnlockDiagnosticVMOptions -XX:+PrintAssembly  查看汇编代码
 */
public class VolatileTest {

    public static volatile int race = 0;

    public static void increase(){
        race ++;
    }

    private static final int THREAD_COUNT = 20;

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 10000; j++) {
                        increase();
                    }
                }
            });
            threads[i].start();
        }

        //等待所有累加线程执行完毕
//        while(Thread.activeCount() > 1){
//            Thread.yield();
//        }
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println(race);

    }

}
