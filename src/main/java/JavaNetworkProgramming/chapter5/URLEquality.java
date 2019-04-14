package JavaNetworkProgramming.chapter5;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 比较URL相等，进行DNS解析
 *
 * @author xuanjian
 */
public class URLEquality {
	public static void main(String[] args) {
		test2();
		//		test1();
	}

	// sameFile()
	private static void test2() {
		try {
			URL u1 = new URL("http://www.ncsa.uiuc.edu/HTMLPrimer.html#GS");
			URL u2 = new URL("http://www.ncsa.uiuc.edu/HTMLPrimer.html#HD");
			// sameFile不比较锚点
			if (u1.sameFile(u2)) {
				System.out.println(u2 + " is the same as " + u1);
			} else {
				System.out.println(u2 + " is not the same as " + u1);
			}
			// 输出 http://www.ncsa.uiuc.edu/HTMLPrimer.html#HD is the same as http://www.ncsa.uiuc.edu/HTMLPrimer.html#GS
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	// equals()
	private static void test1() {
		try {
			URL www = new URL("http://www.ibiblio.org");
			URL ibiblio = new URL("http://ibiblio.org");
			if (www.equals(ibiblio)) {
				System.out.println(ibiblio + " is the same as " + www);
			} else {
				System.out.println(ibiblio + " is not the same as " + www);
			}
			// 输出 http://ibiblio.org is the same as http://www.ibiblio.org
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
