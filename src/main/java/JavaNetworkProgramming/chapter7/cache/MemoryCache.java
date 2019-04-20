package JavaNetworkProgramming.chapter7.cache;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 内存中的ResponseCache
 *
 * @author xuanjian
 */
public class MemoryCache extends ResponseCache {

	private ConcurrentMap<URI, SimpleCacheResponse> responses = new ConcurrentHashMap<>();

	private int maxEntries;

	public MemoryCache() {
		this(100);
	}

	public MemoryCache(int maxEntries) {
		this.maxEntries = maxEntries;
	}

	@Override
	public CacheResponse get(URI uri, String rqstMethod, Map<String, List<String>> rqstHeaders) throws IOException {
		if ("GET".equals(rqstMethod)) {
			SimpleCacheResponse response = responses.get(uri);
			if (response != null && response.isExpired()) {
				responses.remove(uri);
				response = null;
			}
			return response;
		} else {
			return null;
		}
	}

	@Override
	public CacheRequest put(URI uri, URLConnection conn) throws IOException {
		if (responses.size() >= maxEntries) {
			return null;
		}

		CacheControl cacheControl = new CacheControl(conn.getHeaderField("Cache-Control"));
		if (cacheControl.isNoStore()) {
			return null;
		} else if (!conn.getHeaderField(0).startsWith("GET")) {
			return null;
		}

		SimpleCacheRequest request = new SimpleCacheRequest();
		SimpleCacheResponse response = new SimpleCacheResponse(request, cacheControl, conn);
		responses.put(uri, response);
		return request;
	}
}
