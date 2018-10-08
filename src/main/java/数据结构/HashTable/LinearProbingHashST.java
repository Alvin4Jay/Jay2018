package 数据结构.HashTable;

import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

/**
 *  基于线性探测法的散列表
 */
public class LinearProbingHashST<Key, Value>{
    private static final int INIT_CAPACITY = 4;

    private int N; //键值对总数
    private int M; //线性探测表的大小（N < M）
    private Key[] keys; //键集合
    private Value[] values; //值集合

    public LinearProbingHashST(){
        this(INIT_CAPACITY);
    }

    public LinearProbingHashST(int M){
        this.M = M;
        keys = (Key[]) new Object[M];
        values = (Value[]) new Object[M];
    }

    private int hash(Key key){
        return (key.hashCode() & 0x7fffffff) % M;
    }

    //调整大小
    private void resize(int newCapacity){
        LinearProbingHashST<Key, Value> t = new LinearProbingHashST<>(newCapacity);
        for (int i = 0; i < M; i++) {
            if(keys[i] != null){
                t.put(keys[i], values[i]);
            }
        }
        this.M = t.M;
        this.keys = t.keys;
        this.values = t.values;
    }

    public int size(){
        return N;
    }

    public boolean isEmpty(){
        return size() == 0;
    }

    public boolean contains(Key key){
        if (key == null) throw new IllegalArgumentException("argument to contains() is null");

        return get(key) != null;
    }

    //会产生键簇
    public void put(Key key, Value value){
        if (key == null) throw new IllegalArgumentException("first argument to put() is null");

        if(value == null){
            delete(key);
            return;
        }

        // double table size if 50% full
        if (N >= M/2) resize(2*M);

        int i = hash(key);
        for(; keys[i] != null ; i = (i+1) % M){  //数组循环
            if(keys[i].equals(key)) {values[i] = value;return;} //key已存在，更新
        }
        keys[i] = key;
        values[i] = value;
        N ++;
    }

    public Value get(Key key){
        if (key == null) throw new IllegalArgumentException("argument to get() is null");
        //在键簇中寻找
        for(int i=hash(key); keys[i] != null; i = (i+1) % M){
            if(keys[i].equals(key)) return values[i];
        }
        return null;
    }

    //删除
    public void delete(Key key){
        if(key == null) throw new IllegalArgumentException("argument to delete() is null");

        if(!contains(key)) return;
        //找到位置，置为null
        int i= hash(key);
        while(!key.equals(keys[i])){
            i = (i+1) % M;
        }
        //删除
        keys[i] = null;
        values[i] = null;

        //键簇中后面的元素前移
        i = (i+1) % M;
        while(keys[i] != null){
            Key tempK = keys[i];
            Value tempV = values[i];
            keys[i] = null;
            values[i] = null;
            N --;

            put(tempK, tempV);

            i = (i+1) % M;
        }
        N --;

        // halves size of array if it's 12.5% full or less
        if (N > 0 && N <= M/8) resize(M/2);
    }

    public Iterable<Key> keys() {
        Queue<Key> queue = new Queue<Key>();
        for (int i = 0; i < M; i++)
            if (keys[i] != null) queue.enqueue(keys[i]);
        return queue;
    }

    //***********Test************
    public static void main(String[] args) {
        LinearProbingHashST<String, Integer> st = new LinearProbingHashST<String, Integer>();
        for (int i = 0; !StdIn.isEmpty(); i++) {
            String key = StdIn.readString();
            st.put(key, i);
        }

        // print keys
        for (String s : st.keys())
            StdOut.println(s + " " + st.get(s));
    }

}
