package JavaNetworkProgramming.chapter9;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * 多线程daytime服务器
 *
 * @author xuanjian
 */
public class MultiThreadedDaytimeServer {

	private static final int PORT = 13;

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			while (true) {
				try {
					Socket connection = serverSocket.accept();
					DaytimeThread thread = new DaytimeThread(connection);
					thread.start();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		} catch (IOException e) {
			System.err.println("Can't start server");
		}
	}

	private static class DaytimeThread extends Thread {
		private Socket connection;

		public DaytimeThread(Socket connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			try {
				Writer writer = new OutputStreamWriter(connection.getOutputStream());
				Date now = new Date();
				writer.write(now.toString() + "\r\n");
				writer.flush();
			} catch (IOException e) {
				System.err.println(e.getMessage());
			} finally {
				try {
					connection.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
