package JavaNetworkProgramming.chapter8;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 获取Socket的信息
 *
 * @author xuanjian
 */
public class SocketInfo {
	public static void main(String[] args) {

		for (String host : args) {
			try {
				Socket socket = new Socket(host, 80);
				System.out.println(
						"Connected to " + socket.getInetAddress() + " on port " + socket.getPort() + " from port "
								+ socket.getLocalPort() + " of " + socket.getLocalAddress() + "---" + socket
								.toString());
			} catch (UnknownHostException e) {
				System.err.println("I can't find " + host);
			} catch (SocketException e) {
				System.out.println(e.getClass());
				System.err.println("Could not connect to " + host);
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}

	}
}
