package JavaNetworkProgramming.chapter7.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CacheRequest;

/**
 * Cache的一个具体子类
 *
 * @author xuanjian
 */
public class SimpleCacheRequest extends CacheRequest {

	private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

	@Override
	public OutputStream getBody() throws IOException {
		return outputStream;
	}

	@Override
	public void abort() {
		outputStream.reset();
	}

	public byte[] getData() {
		if (outputStream.size() == 0) {
			return null;
		} else {
			return outputStream.toByteArray();
		}
	}
}
