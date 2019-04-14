package JavaNetworkProgramming.chapter5;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 新建URL对象
 *
 * @author xuanjian
 */
public class URLTest {
	public static void main(String[] args) {
		test7();
		//		test6();
		//		test5();
		//		test4();
		//		test3();
		//		test2();
		//		test1();
	}

	/**
	 * URL.getHost()
	 */
	private static void test7() {
		try {
			URL u = new URL("http://tom@www.qq.com");
			String host = u.getHost();
			System.out.println(host);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * URL.openStream方法 try-with-resources
	 */
	private static void test6() {
		try {
			URL u = new URL("http://www.qq.com");
			try (InputStream in = u.openStream()) {
				int c;
				while ((c = in.read()) != -1) {
					System.out.write(c);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * URL.openStream方法
	 */
	private static void test5() {
		InputStream in = null;
		try {
			URL u = new URL("http://www.qq.com");
			in = u.openStream();
			int c;
			while ((c = in.read()) != -1) {
				System.out.write(c);
			}
		} catch (IOException e) {
			System.err.println(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ignored) {
				// 忽略
			}
		}
	}

	/**
	 * 由相对URL构成URL
	 */
	private static void test4() {
		try {
			URL u1 = new URL("http://www.ibiblio.org/javafaq/index.html");
			URL u2 = new URL(u1, "mailinglists.html");
			System.out.println(u2.toString());
		} catch (MalformedURLException e) {
			System.err.println("shouldn't happen; all VMs recognize http");
		}
	}

	/**
	 * 组成部分构成URL
	 */
	private static void test3() {
		try {
			URL url = new URL("http", "www.eff.org", 8000, "/blueribbon.html#intro");
			System.out.println(url.getProtocol());
		} catch (MalformedURLException e) {
			System.err.println("shouldn't happen; all VMs recognize http");
		}
	}

	/**
	 * 由组成部分构成URL
	 */
	private static void test2() {
		try {
			URL url = new URL("http", "www.eff.org", "/blueribbon.html#intro");
			System.out.println(url.getProtocol());
		} catch (MalformedURLException e) {
			System.err.println("shouldn't happen; all VMs recognize http");
		}
	}

	/**
	 * 字符串构成URL
	 */
	private static void test1() {
		try {
			URL url = new URL("https://www.qq.com");
			System.out.println(url.getProtocol());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
