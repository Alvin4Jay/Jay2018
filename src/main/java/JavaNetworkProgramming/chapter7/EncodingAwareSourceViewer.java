package JavaNetworkProgramming.chapter7;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 用正确的字符集下载一个web页面
 *
 * @author xuanjian
 */
public class EncodingAwareSourceViewer {
	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				// 编码
				String encoding = "utf-8";
				// 打开URLConnection进行读取
				URL u = new URL(args[0]);
				URLConnection uc = u.openConnection();
				uc.connect();
				String contentType = uc.getContentType();
				int encodingStart = contentType.indexOf("charset=");
				if (encodingStart != -1) {
					encoding = contentType.substring(encodingStart + 8);
				}
				// 缓冲输入以提高性能
				InputStream in = new BufferedInputStream(uc.getInputStream());
				// 转换为Reader
				Reader r = new InputStreamReader(in, encoding);
				int c;
				while ((c = r.read()) != -1) {
					System.out.print((char) c);
				}
				r.close();
			} catch (MalformedURLException e) {
				System.err.println(args[0] + " is not a parseable URL");
			} catch (UnsupportedEncodingException e) {
				System.out.println("Server sent an encoding Java does not supported: " + e.getMessage());
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
