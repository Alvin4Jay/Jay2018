package JDKSourceFileAnalysis.Collection.LinkedHashMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 利用LinkedHashMap实现LRU算法
 *
 * @author xuanjian
 */
public class LRU<K, V> extends LinkedHashMap<K, V> implements Map<K, V> {
	private static final long serialVersionUID = 1L;

	public LRU(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor, accessOrder);
	}

	// 重写LinkedHashMap中的removeEldestEntry方法，当LRU中元素多余6个时，删除最不经常使用的元素
	@Override
	protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
		if (size() > 6) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		LRU<Character, Integer> lru = new LRU<Character, Integer>(16, 0.75f, true);

		String s = "abcdefghijkl";
		for (int i = 0; i < s.length(); i++) {
			lru.put(s.charAt(i), i);
		}
		System.out.println("LRU中key为h的Entry的值为： " + lru.get('h'));
		System.out.println("LRU的大小 ：" + lru.size());
		System.out.println("LRU ：" + lru);
	}
}
