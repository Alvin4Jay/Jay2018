package JDKSourceFileAnalysis.JUCLocks;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  交换数据（Exchanger）
 */
public class ExchangerTest {
    public static void main(String[] args) {
        ExecutorService service = Executors.newCachedThreadPool();
        final Exchanger<String> exchanger = new Exchanger<String>(); //String 交换的数据类型

        service.execute(new Runnable(){
            @Override
            public void run(){
                try {
                    String data1 = "零食";
                    System.out.println("线程" + Thread.currentThread().getName() + "正在把数据" + data1 + "换出去");
                    Thread.sleep((long) (Math.random() * 1000));
                    String data2 = exchanger.exchange(data1);
                    System.out.println("线程" + Thread.currentThread().getName() + "换回的数据为" + data2);
                } catch (Exception e) {

                }
            }
        });

        service.execute(new Runnable(){
            @Override
            public void run(){
                try {
                    String data1 = "钱";
                    System.out.println("线程" + Thread.currentThread().getName() + "正在把数据" + data1 + "换出去");
                    Thread.sleep((long) (Math.random() * 1000));
                    String data2 = exchanger.exchange(data1);
                    System.out.println("线程" + Thread.currentThread().getName() + "换回的数据为" + data2);
                } catch (Exception e) {

                }
            }
        });

        service.shutdown();
    }
}