package JavaMultiThread.JavaConcurrencyInPractice.Chapter4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  4.4.1客户端加锁，使用List对象自身的内置锁
 */
public class ListHelper <E>{
    public List<E> list = Collections.synchronizedList(new ArrayList<E>());

    public boolean putIfAbsent(E x){
        synchronized (list){  //对象内置锁
            boolean absent = !list.contains(x);
            if(absent){
                list.add(x);
            }
            return absent;
        }
    }

}
