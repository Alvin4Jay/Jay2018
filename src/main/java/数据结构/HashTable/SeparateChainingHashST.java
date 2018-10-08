package 数据结构.HashTable;

import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.SequentialSearchST;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;

/**
 * 基于拉链法的散列表
 */
public class SeparateChainingHashST<Key, Value> {
    private static final int INIT_CAPACITY = 4;

    private int n; //键值对总数
    private int m; //散列表大小 (数组大小) (n > m)
    private SequentialSearchST<Key, Value>[] st;  //存放链表对象的数组

    public SeparateChainingHashST() {
        this(INIT_CAPACITY);
    }

    public SeparateChainingHashST(int m) {
        //创建m条链表
        this.m = m;
        //初始化
        st = (SequentialSearchST<Key, Value>[])new SequentialSearchST[m];
        for (int i = 0; i < m; i++) {
            st[i] = new SequentialSearchST();
        }
    }

    //调整大小
    private void resize(int newCapacity){
        SeparateChainingHashST<Key, Value> temp = new SeparateChainingHashST<Key, Value>(newCapacity);

        for (int i = 0; i < m; i++) {
            for (Key key : st[i].keys()) {
                temp.put(key, st[i].get(key));
            }
        }

        //n不变
        this.n  = temp.n;
        this.m = temp.m;
        this.st = temp.st;
    }

    //hash函数,决定用哪个st[i],重要
    private int hash(Key key){
        return (key.hashCode() & 0x7fffffff) % m;
    }

    public int size(){
        return n;
    }

    public boolean isEmpty(){
        return size() == 0;
    }

    public boolean contains(Key key){
        if (key == null) throw new IllegalArgumentException("argument to contains() is null");

        return get(key) != null;
    }

    //查找
    public Value get(Key key){
        if (key == null) throw new IllegalArgumentException("argument to get() is null");

        int i = hash(key);
        return st[i].get(key);
    }

    //插入
    public void put(Key key, Value value){
        if (key == null) throw new IllegalArgumentException("first argument to put() is null");

        if(value == null){
            delete(key);
            return;
        }

        //判断是否超容
        // double table size if average length of list >= 10
        if(n >= 10*m) resize(2*m);

        int i = hash(key);
        if(!st[i].contains(key)) n++; //如果散列表中不包含该键，n++
        st[i].put(key, value);
    }

    //删除
    public void delete(Key key){
        if (key == null) throw new IllegalArgumentException("argument to delete() is null");

        int i = hash(key);
        if(st[i].contains(key)) n--; //存在，并且要删除的，总数减一
        st[i].delete(key); //执行删除，无论存不存在

        //判断是否太小
        // halve table size if average length of list <= 2
        if (m > INIT_CAPACITY && n <= 2*m) resize(m/2);
    }

    public Iterable<Key> keys(){
        Queue<Key> queue = new Queue<Key>();

        for (int i = 0; i < m; i++) {
            for (Key key : st[i].keys()) {
                queue.enqueue(key);
            }
        }
        return queue;
    }

    //*****************test*******************
    public static void main(String[] args) {
        SeparateChainingHashST<String, Integer> st = new SeparateChainingHashST<String, Integer>();
        for (int i = 0; !StdIn.isEmpty(); i++) {
            String key = StdIn.readString();
            st.put(key, i);
        }

        // print keys
        for (String s : st.keys())
            StdOut.println(s + " " + st.get(s));

    }

}
