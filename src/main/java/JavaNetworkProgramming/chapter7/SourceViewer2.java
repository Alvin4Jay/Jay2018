package JavaNetworkProgramming.chapter7;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 用URLConnection下载一个web页面
 *
 * @author xuanjian
 */
public class SourceViewer2 {
	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				// 打开URLConnection进行读取
				URL u = new URL(args[0]);
				URLConnection uc = u.openConnection();
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
