package JavaMultiThread.Chapter5;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Fast Fail机制
 */
public class FastFailTest {
    //private static List<String> list = new ArrayList<String>();
    private static List<String> list = new CopyOnWriteArrayList<String>();

    //迭代
    private static void printAll(){
        System.out.println("");

        String value = null;
        Iterator<String> it = list.iterator();
        while(it.hasNext()){
            value = it.next();
            System.out.print(value + ", ");
        }
    }

    //向list中依次添加0,1,2,3,4,5，每添加一个数之后，就通过printAll()遍历整个list
    private static class ThreadOne extends Thread{
        @Override
        public void run() {
            int i = 0;
            while(i < 6){
                list.add(String.valueOf(i));
                printAll();
                i ++;
            }
        }
    }

    //向list中依次添加10,11,12,13,14,15，每添加一个数之后，就通过printAll()遍历整个list
    private static class ThreadTwo extends Thread{
        @Override
        public void run() {
            int i = 10;
            while(i < 16){
                list.add(String.valueOf(i));
                printAll();
                i ++;
            }
        }
    }

    public static void main(String[] args) {
        new ThreadOne().start();
        new ThreadTwo().start();
    }

}

