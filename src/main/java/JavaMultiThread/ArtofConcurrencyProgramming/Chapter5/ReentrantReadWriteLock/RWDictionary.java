package JavaMultiThread.ArtofConcurrencyProgramming.Chapter5.ReentrantReadWriteLock;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * RWDictionary 读操作显著多于写操作，适合使用ReentrantReadWriteLock控制并发
 *
 * @author xuanjian.xuwj
 */
public class RWDictionary {
	private final Map<String, Data> m = new TreeMap<String, Data>();
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	public Data get(String key) {
		r.lock();
		try { return m.get(key); } finally { r.unlock(); }
	}

	public String[] allKeys() {
		r.lock();
		try { return m.keySet().toArray(new String[0]); } finally { r.unlock(); }
	}

	public Data put(String key, Data value) {
		w.lock();
		try { return m.put(key, value); } finally { w.unlock(); }
	}

	public void clear() {
		w.lock();
		try { m.clear(); } finally { w.unlock(); }
	}

	private static class Data {
	}
}
