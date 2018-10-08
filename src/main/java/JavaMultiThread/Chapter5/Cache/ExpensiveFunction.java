package JavaMultiThread.Chapter5.Cache;

import java.math.BigInteger;

/**
 * Created by Jay on 2017/6/6.
 */
public class ExpensiveFunction implements Computable<String, BigInteger>{
    @Override
    public BigInteger compute(String arg) throws InterruptedException {
        //经过很长的时间计算之后
        return new BigInteger(arg);
    }
}
