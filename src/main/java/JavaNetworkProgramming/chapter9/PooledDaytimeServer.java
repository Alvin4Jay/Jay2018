package JavaNetworkProgramming.chapter9;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 使用线程池的daytime服务器
 *
 * @author xuanjian
 */
public class PooledDaytimeServer {

	private static final int PORT = 13;

	private static ExecutorService pool = Executors.newFixedThreadPool(50);

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			while (true) {
				try {
					Socket connection = serverSocket.accept();
					Callable<Void> task = new DaytimeTask(connection);
					pool.submit(task);
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		} catch (IOException e) {
			System.err.println("Can't start server");
		}
	}

	private static class DaytimeTask implements Callable<Void> {
		private Socket connection;

		public DaytimeTask(Socket connection) {
			this.connection = connection;
		}

		@Override
		public Void call() throws Exception {
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
			return null;
		}
	}

}
