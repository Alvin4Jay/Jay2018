package JDKSourceFileAnalysis.Producer_Consumer_Pattern;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BlockingQueue实现生产者消费者模式
 */
public class ProducerConsumerPattern {
    public static void main(String args[]){

        //阻塞队列
        BlockingQueue sharedQueue = new LinkedBlockingQueue();

        //Creating Producer and Consumer Thread
        Thread prodThread1 = new Thread(new ProducerDemo(sharedQueue), "生产者1");
        Thread prodThread2 = new Thread(new ProducerDemo(sharedQueue), "生产者2");
        Thread consThread = new Thread(new ConsumerDemo(sharedQueue));

        //Starting producer and Consumer thread
        prodThread1.start();
        prodThread2.start();
        consThread.start();
    }
}

//生产者
class ProducerDemo implements Runnable {

    private final BlockingQueue sharedQueue;

    public ProducerDemo(BlockingQueue sharedQueue) {
        this.sharedQueue = sharedQueue;
    }

    @Override
    public void run() {
        for(int i=0; i<10; i++){
            try {
                System.out.println(Thread.currentThread().getName() + "Produced: " + i);
                sharedQueue.put(i);
                Thread.sleep(1000);  //每隔1s放一个 put
            } catch (InterruptedException ex) {
                Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

//消费者
class ConsumerDemo implements Runnable{

    private final BlockingQueue sharedQueue;

    public ConsumerDemo (BlockingQueue sharedQueue) {
        this.sharedQueue = sharedQueue;
    }

    @Override
    public void run() {
        while(true){
            try {
                System.out.println("Consumed: "+ sharedQueue.take()); //take
            } catch (InterruptedException ex) {
                Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
