package JavaNetworkProgramming.chapter5;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 获取网页源码
 *
 * @author xuanjian
 */
public class SourceViewer {
	public static void main(String[] args) {
		if (args.length > 0) {
			InputStream in = null;
			try {
				// 打开URL进行读取
				URL u = new URL(args[0]);
				in = u.openStream();
				// 缓冲输入以提高性能
				in = new BufferedInputStream(in);
				// 转换为Reader
				Reader r = new InputStreamReader(in);
				int c;
				while ((c = r.read()) != -1) {
					System.out.print((char) c);
				}
			} catch (MalformedURLException e) {
				System.err.println(args[0] + " is not a parseable URL");
			} catch (IOException e) {
				System.err.println(e.getMessage());
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
