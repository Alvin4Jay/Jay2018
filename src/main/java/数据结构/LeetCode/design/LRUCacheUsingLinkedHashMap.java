package 数据结构.LeetCode.design;

import java.util.Map;

/**
 * LRUCache
 */
public class LRUCacheUsingLinkedHashMap {

    private int capacity;
    private Map<Integer, Integer> cache;

    public LRUCacheUsingLinkedHashMap(int capacity) {
        this.capacity = capacity;
        //按照访问顺序排序
        this.cache = new java.util.LinkedHashMap<Integer, Integer> (capacity, 0.75f, true) {
            // 定义put后的移除规则，大于容量就删除eldest
            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > capacity;
            }
        };
    }

    public int get(int key) {
        return cache.getOrDefault(key, -1);
    }

    public void put(int key, int value) {
        cache.put(key, value);
    }
}
