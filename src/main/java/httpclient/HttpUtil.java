package httpclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * http client
 */
public class HttpUtil {

	private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

	public static String invokeURL(String urlString, int timeout, int readtimeout) {
		HttpURLConnection conn = null;
		URL url = null;
		try {
			url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(readtimeout);
			conn.setRequestMethod("GET");
			conn.connect();
			InputStream urlStream = conn.getInputStream();
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(urlStream));
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
			return sb.toString();

		} catch (Exception e) {
			log.error("http调用失败,url=" + urlString, e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return "error";
	}

	static public String httpGet(String url, Map<String, String> headers, Map<String, String> paramValues,
			String encoding, long readTimeoutMs) throws IOException {
		String encodedContent = encodingParams(paramValues, encoding);
		url += (null == encodedContent) ? "" : ("?" + encodedContent);

		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout((int) readTimeoutMs);
			setHeaders(conn, headers, encoding);

			conn.connect();
			int respCode = conn.getResponseCode();

			if (HttpURLConnection.HTTP_OK == respCode) {
				return toString(conn.getInputStream(), encoding);
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("httpGet调用失败,url=" + url, e);
			return "error";
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	static public String httpPost(String url, Map<String, String> headers, Map<String, String> paramValues,
			String encoding, long readTimeoutMs) throws IOException {
		String encodedContent = encodingParams(paramValues, encoding);

		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout((int) readTimeoutMs);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			setHeaders(conn, headers, encoding);

			conn.getOutputStream().write(encodedContent.getBytes());

			int respCode = conn.getResponseCode();

			if (HttpURLConnection.HTTP_OK == respCode) {
				return toString(conn.getInputStream(), encoding);
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("httpPost调用失败,url=" + url, e);
			return "error";
		} finally {
			if (null != conn) {
				conn.disconnect();
			}
		}

	}

	public static String httpPostJSON(String url, Map<String, String> headers, String body, String encoding,
			long readTimeoutMs) throws IOException {

		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout((int) readTimeoutMs);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			setJSONHeaders(conn, headers, encoding);

			conn.getOutputStream().write(body.getBytes(encoding));
			int respCode = conn.getResponseCode();

			if (HttpURLConnection.HTTP_OK == respCode) {
				return toString(conn.getInputStream(), encoding);
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("httpPost调用失败,url=" + url, e);
			return "error";
		} finally {
			if (null != conn) {
				conn.disconnect();
			}
		}
	}

	static private void setJSONHeaders(HttpURLConnection conn, Map<String, String> headers, String encoding) {
		if (null != headers) {
			for (Iterator<String> iter = headers.keySet().iterator(); iter.hasNext(); ) {
				String key = iter.next();
				conn.addRequestProperty(key, headers.get(key));
			}
		}
		conn.addRequestProperty("Content-Type", "application/json;charset=" + encoding);
	}

	static private void setHeaders(HttpURLConnection conn, Map<String, String> headers, String encoding) {
		if (null != headers) {
			for (Iterator<String> iter = headers.keySet().iterator(); iter.hasNext(); ) {
				String key = iter.next();
				conn.addRequestProperty(key, headers.get(key));
			}
		}
		conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding);
	}

	static private String encodingParams(Map<String, String> urlParams, String encoding)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		if (null == urlParams) {
			return null;
		}

		for (Iterator<String> iter = urlParams.keySet().iterator(); iter.hasNext(); ) {
			String key = iter.next();
			sb.append(key).append("=");
			sb.append(URLEncoder.encode(urlParams.get(key), encoding));
			if (iter.hasNext()) {
				sb.append("&");
			}
		}
		return sb.toString();
	}

	static public String toString(InputStream input, String encoding) throws IOException {
		return (null == encoding) ?
				toString(new InputStreamReader(input)) :
				toString(new InputStreamReader(input, encoding));
	}

	static public String toString(Reader reader) throws IOException {
		CharArrayWriter sw = new CharArrayWriter();
		copy(reader, sw);
		return sw.toString();
	}

	static public long copy(Reader input, Writer output) throws IOException {
		char[] buffer = new char[1 << 12];
		long count = 0;
		for (int n = 0; (n = input.read(buffer)) >= 0; ) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	static public String getUrl(String host, String port, String relativePath) {
		return new StringBuilder().append("http://").append(host + ":" + port).append(relativePath).toString();
	}

}
