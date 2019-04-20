package JavaNetworkProgramming.chapter7.httpurlconnection;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 用HTTPURLConnection下载一个web页面，包括响应行、响应头
 *
 * @author xuanjian
 */
public class SourceViewer3 {
	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				// 打开URLConnection进行读取
				URL u = new URL(args[0]);
				HttpURLConnection uc = (HttpURLConnection) u.openConnection();
				// 响应行
				int responseCode = uc.getResponseCode();
				String responseMessage = uc.getResponseMessage();
				System.out.println("HTTP/1.x " + responseCode + " " + responseMessage);
				//				System.err.println(uc.getHeaderField(0));

				// 响应头
				for (int j = 1; ; j++) {
					String value = uc.getHeaderField(j);
					String key = uc.getHeaderFieldKey(j);
					if (key == null || value == null) {
						break;
					}
					System.out.println(key + ": " + value);
				}
				System.out.println();

				// 响应体
				try (InputStream in = uc.getInputStream()) { // 自动关闭
					// 缓冲输入以提高性能
					InputStream buffer = new BufferedInputStream(in);
					// 转换为Reader
					Reader r = new InputStreamReader(buffer);
					int c;
					while ((c = r.read()) != -1) {
						System.out.print((char) c);
					}
				}
			} catch (MalformedURLException e) {
				System.err.println(args[0] + " is not a parseable URL");
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
