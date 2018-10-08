package 数据结构.LeetCode.design;

import java.util.HashMap;
import java.util.Map;

/**
 *  LRUCache
 */
public class LRUCache {
    //构造双向链表节点
    class Node {
        Node pre;
        Node next;
        Integer key;
        Integer val;

        Node(Integer k, Integer v) {
            key = k;
            val = v;
        }
    }
    //判断元素是否存在时用
    private Map<Integer, Node> map = new HashMap<Integer, Node>();
    // 双向链表头部，最旧
    Node head;
    // 双向链表尾部，最新
    Node tail;
    //容量
    int cap;

    public LRUCache(int capacity) {
        cap = capacity;
        head = new Node(null, null);
        tail = new Node(null, null);
        head.next = tail;
        tail.pre = head;
    }

    /**
     * 查看是否存在该元素，不存在返回-1，存在(先移出该元素，再将其放到链表尾部最新位置)
     * @param key
     * @return
     */
    public int get(int key) {
        Node n = map.get(key);
        if(n!=null) {
            n.pre.next = n.next;
            n.next.pre = n.pre;
            n.pre = null;
            n.next = null;
            appendTail(n); //放到双向链表尾部(最新位置)
            return n.val;
        }
        return -1; //不存在
    }

    public void put(int key, int value) {
        Node n = map.get(key);
        // existed
        if(n!=null) {  //key已存在，则替换val为新值
            n.val = value;
            map.put(key, n);
            n.pre.next = n.next;
            n.next.pre = n.pre;
            n.pre = null;
            n.next = null;
            appendTail(n); //放到尾部
            return;
        }
        // else {
        if(map.size() == cap) { //满了则先移除最旧的元素节点
            Node tmp = head.next;
            head.next = head.next.next;
            head.next.pre = head;
            tmp.pre = null;
            tmp.next = null;
            map.remove(tmp.key); //移除
        }
        n = new Node(key, value);
        // youngest node append taill
        appendTail(n); //放到链表尾部
        map.put(key, n);
    }

    //将节点放到链表尾部
    private void appendTail(Node n) {
        n.next = tail;
        n.pre = tail.pre;
        tail.pre.next = n;
        tail.pre = n;
    }

    public static void main(String[] args) {
        LRUCache cache = new LRUCache( 2 /* capacity */ );

        cache.put(1, 1);
        cache.put(2, 2);
        System.out.println(cache.get(1));       // returns 1
        cache.put(3, 3);    // evicts key 2
        System.out.println(cache.get(2));       // returns -1 (not found)
        cache.put(4, 4);    // evicts key 1
        System.out.println(cache.get(1));       // returns -1 (not found)
        System.out.println(cache.get(3));       // returns 3
        System.out.println(cache.get(4));       // returns 4


    }


/**
 * Your LRUCache object will be instantiated and called as such:
 * LRUCache obj = new LRUCache(capacity);
 * int param_1 = obj.get(key);
 * obj.put(key,value);
 */
}
