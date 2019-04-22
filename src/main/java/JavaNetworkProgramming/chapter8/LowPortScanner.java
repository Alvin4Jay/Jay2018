package JavaNetworkProgramming.chapter8;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 查看指定主机上前1024端口中哪些安装有TCP服务器
 *
 * @author xuanjian
 */
public class LowPortScanner {

	public static void main(String[] args) {

		String host = args.length > 0 ? args[0] : "localhost";

		for (int i = 1; i < 1024; i++) {
			try {
				Socket socket = new Socket(host, i);
				System.out.println("There is a server on port " + i + " of " + host);
				socket.close();
			} catch (UnknownHostException e) {
				System.err.println(e.getMessage());
				break;
			} catch (IOException e) {
				// ConnectException
				//				System.out.println(e.getClass());
				// 这个端口上不是一个服务器
			}
		}

	}
}
