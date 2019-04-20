package JavaNetworkProgramming.chapter7;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 打开URLConnection
 *
 * @author xuanjian
 */
public class OpenURLConnection {

	public static void main(String[] args) {
		try {
			URL u = new URL("http://www.qq.com");
			// 打开URLConnection
			URLConnection connection = u.openConnection();
			// 从URL读取数据。。。
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
