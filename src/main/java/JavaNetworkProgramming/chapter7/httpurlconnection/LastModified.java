package JavaNetworkProgramming.chapter7.httpurlconnection;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * 询问服务端，获得URL的最后一个修改的时间
 *
 * @author xuanjian
 */
public class LastModified {
	public static void main(String[] args) {
		for (String arg : args) {
			try {
				URL u = new URL(arg);
				HttpURLConnection http = (HttpURLConnection) u.openConnection();
				http.setRequestMethod("HEAD");
				// 发送HEAD请求
				System.out.println(u + " was last modified at " + new Date(http.getLastModified()));
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}
}
