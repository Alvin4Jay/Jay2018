package JavaNetworkProgramming.chapter5;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 下载一个对象
 *
 * @author xuanjian
 */
public class ContentGetter {
	public static void main(String[] args) {
		if (args.length > 0) {
			// 打开URL进行读取
			try {
				URL u = new URL(args[0]);
				Object o = u.getContent();
				System.out.println("I got a " + o.getClass().getName());
			} catch (MalformedURLException e) {
				System.err.println(args[0] + " is not a parseable URL");
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

}
