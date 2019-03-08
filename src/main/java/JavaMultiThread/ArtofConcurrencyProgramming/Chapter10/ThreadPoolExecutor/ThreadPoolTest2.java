package JavaMultiThread.ArtofConcurrencyProgramming.Chapter10.ThreadPoolExecutor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *  ThreadPoolExecutor 状态字段测试
 */
public class ThreadPoolTest2 {
    private static final int COUNT_BITS = Integer.SIZE - 3;  //29
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1; //00011111111111111111111111111

    // runState is stored in the high-order bits
    private static final int RUNNING    = -1 << COUNT_BITS; //11100000000000000000000000000000
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    private static final int STOP       =  1 << COUNT_BITS;
    private static final int TIDYING    =  2 << COUNT_BITS;
    private static final int TERMINATED =  3 << COUNT_BITS;

    // Packing and unpacking ctl
    private static int runStateOf(int c)     { return c & ~CAPACITY; }
    private static int workerCountOf(int c)  { return c & CAPACITY; }
    private static int ctlOf(int rs, int wc) { return rs | wc; }

    public static void main(String[] args) {
        System.out.println("CAPACITY_Binary:" + Integer.toBinaryString(CAPACITY));
        System.out.println("CAPACITY_Decimal:" + CAPACITY);
        System.out.println("RUNNING_Binary:" + Integer.toBinaryString(RUNNING));
        System.out.println("RUNNING_Decimal:" + RUNNING);
        System.out.println("SHUTDOWN_Binary:" + Integer.toBinaryString(SHUTDOWN));
        System.out.println("SHUTDOWN_Decimal:" + SHUTDOWN);
        System.out.println("STOP_Binary:" + Integer.toBinaryString(STOP));
        System.out.println("STOP_Decimal:" + STOP);
        System.out.println("TIDYING_Binary:" + Integer.toBinaryString(TIDYING));
        System.out.println("TIDYING_Decimal:" + TIDYING);
        System.out.println("TERMINATED_Binary:" + Integer.toBinaryString(TERMINATED));
        System.out.println("TERMINATED_Decimal:" + TERMINATED);

        AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));  //11100000000000000000000000000000
        System.out.println("c: " + ctl.get());  //-536870912
        System.out.println("c:" + Integer.toBinaryString(ctl.get()));  //11100000000000000000000000000000
        System.out.println("runStateOf:" + Integer.toBinaryString(runStateOf(ctl.get()))); //线程池状态RUNNING
        System.out.println("workerCountOf_正在执行的线程数:" + Integer.toBinaryString(workerCountOf(ctl.get()))); //0

        int c = ctl.get();
        System.out.println(c);
        int rs = runStateOf(c);
        if (rs >= SHUTDOWN && !(rs == SHUTDOWN)) { //RUNNING
            System.out.println("--------");
        }

        //下面对c进行加1，来判断正在执行的线程数
        ctl.compareAndSet(c, c + 1); //11100000000000000000000000000001
        c = ctl.get();
        System.out.println("c2:" + Integer.toBinaryString(c));  //11100000000000000000000000000001
        System.out.println("runStateOf2:" + Integer.toBinaryString(runStateOf(c))); //11100000000000000000000000000000 RUNNING
        System.out.println("workerCountOf2_RUNNING_THREAD:" + Integer.toBinaryString(workerCountOf(c)));
    }
}
