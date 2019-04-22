package JavaNetworkProgramming.chapter8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Daytime协议客户端
 *
 * @author xuanjian
 */
public class DaytimeClient {
	public static void main(String[] args) {

		String hostname = args.length > 0 ? args[0] : "time.nist.gov";

		Socket socket = null;

		try {
			socket = new Socket(hostname, 13);
			socket.setSoTimeout(15_000); // 设置读超时
			InputStream in = socket.getInputStream();
			InputStreamReader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);
			StringBuilder time = new StringBuilder();
			for (int c = reader.read(); c != -1; c = reader.read()) {
				time.append((char) c);
			}
			System.out.println(time.toString());
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ignored) {

				}
			}
		}

	}
}
