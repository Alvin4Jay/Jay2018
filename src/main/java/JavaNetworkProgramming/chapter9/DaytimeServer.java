package JavaNetworkProgramming.chapter9;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * daytime服务器
 *
 * @author xuanjian
 */
public class DaytimeServer {

	private static final int PORT = 13;

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			while (true) {
				try (Socket socket = serverSocket.accept()) {
					Writer out = new OutputStreamWriter(socket.getOutputStream());
					Date now = new Date();
					out.write(now.toString() + "\r\n");
					out.flush();
				} catch (IOException ignored) {}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
