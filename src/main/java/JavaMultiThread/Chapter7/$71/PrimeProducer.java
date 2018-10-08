package JavaMultiThread.Chapter7.$71;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;

/**
 *  中断
 */
public class PrimeProducer extends Thread{
    private final BlockingQueue<BigInteger> queue;

    public PrimeProducer(BlockingQueue<BigInteger> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            BigInteger p  =BigInteger.ONE;
            while (!Thread.currentThread().isInterrupted()){ //标志位检查
                queue.put(p = p.nextProbablePrime()); //有可能阻塞
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void cancel(){
        interrupt(); //产生中断
    }
}
