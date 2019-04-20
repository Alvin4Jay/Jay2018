package JavaNetworkProgramming.chapter7;

import JavaNetworkProgramming.chapter5.QueryString;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

/**
 * POST提交表单
 *
 * @author xuanjian
 */
public class FormPoster {

	private URL url;

	private QueryString queryString = new QueryString();

	public FormPoster(URL url) {
		if (!url.getProtocol().startsWith("http")) {
			throw new IllegalArgumentException("Posting only works for http URLs");
		}
		this.url = url;
	}

	public void add(String name, String value) {
		queryString.add(name, value);
	}

	public URL getUrl() {
		return this.url;
	}

	/**
	 * @return InputStream响应
	 */
	public InputStream post() throws IOException {
		URLConnection uc = url.openConnection();
		uc.setDoOutput(true);
		try (OutputStreamWriter writer = new OutputStreamWriter(uc.getOutputStream(), StandardCharsets.UTF_8)) {
			// POST请求行、请求头由URLConnection发送，这里只需要发送数据
			writer.write(queryString.getQuery());
			writer.write("\r\n");
			writer.flush();
		}
		return uc.getInputStream();
	}

	public static void main(String[] args) {
		URL url;
		if (args.length > 0) {
			try {
				url = new URL(args[0]);
			} catch (MalformedURLException e) {
				System.err.println("Usage: java FormPoster url");
				return;
			}
		} else {
			try {
				url = new URL("http://www.cafeaulait.org/books/jnp4/postquery.phtml");
			} catch (MalformedURLException e) {
				System.err.println(e);
				return;
			}
		}

		FormPoster poster = new FormPoster(url);
		poster.add("name", "Elliotte Rusty Harold");
		poster.add("email", "elharo@ibilio.org");

		try (InputStream in = poster.post()) {
			Reader r = new InputStreamReader(in);
			int c;
			while ((c = r.read()) != -1) {
				System.out.print((char) c);
			}
			System.out.println();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

}
