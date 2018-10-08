package JavaMultiThread.Chapter7.$71;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *  枚举素数
 */
public class PrimeGenerator implements Runnable{
    private final List<BigInteger> primes = new ArrayList<BigInteger>();

    private volatile boolean cancelled;

    @Override
    public void run() {
        BigInteger p = BigInteger.ONE;

        while (!cancelled){
            p = p.nextProbablePrime(); //大于1的素数
            synchronized (this){
                primes.add(p);
            }
        }
    }

    public void cancel(){
        cancelled = true;
    }

    public synchronized List<BigInteger> get(){
        return new ArrayList<BigInteger>(primes);
    }


}
