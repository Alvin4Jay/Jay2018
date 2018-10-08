package JavaMultiThread.Chapter4;

import net.jcip.annotations.ThreadSafe;

import java.util.Vector;

/**
 *  扩展Vector类并增加一个"若没有则添加"的方法
 */
@ThreadSafe
public class BetterVector<E> extends Vector<E>{
    public synchronized boolean putIfAbsent(E x){
        boolean absent = !contains(x);
        if(absent){
            add(x);
        }
        return absent;
    }
}
