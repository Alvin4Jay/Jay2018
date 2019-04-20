package JavaNetworkProgramming.chapter7.httpurlconnection;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 用URLConnection下载一个web页面
 *
 * @author xuanjian
 */
public class SourceViewer4 {
	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				// 打开URLConnection进行读取
				URL u = new URL(args[0]);
				HttpURLConnection uc = (HttpURLConnection) u.openConnection();
				try (InputStream raw = uc.getInputStream()) {
					printFromStream(raw);
				} catch (IOException e) {
					// 读取错误流
					printFromStream(uc.getErrorStream());
				}
			} catch (MalformedURLException e) {
				System.err.println(args[0] + " is not a parseable URL");
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

	private static void printFromStream(InputStream raw) throws IOException {
		try (InputStream buffer = new BufferedInputStream(raw)) {
			Reader r = new InputStreamReader(buffer);
			int c;
			while ((c = r.read()) != -1) {
				System.out.print((char) c);
			}
		}
	}
}
