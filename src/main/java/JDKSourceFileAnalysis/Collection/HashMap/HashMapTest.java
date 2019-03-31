package JDKSourceFileAnalysis.Collection.HashMap;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * HashMap测试
 */
public class HashMapTest {
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put(null, "123");
        map.put("123", null);

        System.out.println(map.get(null));
        System.out.println(map.containsValue(null));

//        System.out.println(tableSizeFor(14));

        //HashTable K V不能为null
        Hashtable<String, String> ht = new Hashtable<>();
		//        ht.put("123", null);
		//        ht.put(null, "123");

		System.out.println(tableSizeFor(18));


	}

    //计算大于cap的2的幂次
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    static final int MAXIMUM_CAPACITY = 1 << 30;
}
