package JavaMultiThread.Chapter3;

/**
 * 先餐券的可变整数类
 */
public class SynchronizedInteger {
    private int value;

    public synchronized int get(){
        return value;
    }

    public synchronized void set(int value){
        this.value = value;
    }

}
