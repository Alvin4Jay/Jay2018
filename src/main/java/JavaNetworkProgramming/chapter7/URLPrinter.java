package JavaNetworkProgramming.chapter7;

import java.net.URL;
import java.net.URLConnection;

/**
 * 显示指向www.qq.com的URLConnection的URL
 *
 * @author xuanjian
 */
public class URLPrinter {
	public static void main(String[] args) {
		try {
			URL u = new URL("http://www.qq.com");
			URLConnection connection = u.openConnection();
			System.out.println(connection.getURL());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
