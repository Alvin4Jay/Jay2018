package JavaNetworkProgramming.chapter7;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

/**
 * 将ifModifiedSince设置为24小时之前
 *
 * @author xuanjian
 */
public class Last24 {
	public static void main(String[] args) {
		Date today = new Date();
		long millisecondsPerDay = 24 * 60 * 60 * 1_000;
		for (String arg : args) {
			try {
				URL u = new URL(arg);
				URLConnection connection = u.openConnection();
				System.out.println("Original if modified since: " + new Date(connection.getIfModifiedSince()));
				connection.setIfModifiedSince(today.getTime() - millisecondsPerDay);
				System.out.println(
						"Will receive file if ti's modified since: " + new Date(connection.getIfModifiedSince()));
				try (InputStream in = connection.getInputStream()) {
					InputStream inputStream = new BufferedInputStream(in);
					Reader r = new InputStreamReader(inputStream);
					int c;
					while ((c = r.read()) != -1) {
						System.out.print((char) c);
					}
					System.out.println();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
