package JavaNetworkProgramming.chapter7.cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CacheResponse;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * CacheResponse的一个具体子类
 *
 * @author xuanjian
 */
public class SimpleCacheResponse extends CacheResponse {

	private final Map<String, List<String>> headers;

	private final CacheControl cacheControl;

	private final SimpleCacheRequest request;

	private final Date expires;

	public SimpleCacheResponse(SimpleCacheRequest request, CacheControl cacheControl, URLConnection uc) {
		this.cacheControl = cacheControl;
		this.request = request;
		this.headers = Collections.unmodifiableMap(uc.getHeaderFields());
		this.expires = new Date(uc.getExpiration());
	}

	@Override
	public InputStream getBody() throws IOException {
		return new ByteArrayInputStream(request.getData());
	}

	@Override
	public Map<String, List<String>> getHeaders() throws IOException {
		return headers;
	}

	public CacheControl getCacheControl() {
		return cacheControl;
	}

	public boolean isExpired() {
		Date now = new Date();
		if (cacheControl.getMaxAge().before(now)) {
			return false;
		} else if (expires != null && cacheControl.getMaxAge() != null) {
			return expires.before(now);
		} else {
			return false;
		}
	}

}
