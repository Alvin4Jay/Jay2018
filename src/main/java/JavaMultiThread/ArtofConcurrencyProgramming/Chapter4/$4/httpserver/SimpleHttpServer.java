package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$4.httpserver;

import JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$4.threadpool.ThreadPool;
import JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$4.threadpool.ThreadPoolImpl;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 简单的Web服务器
 *
 * @author xuanjian.xuwj
 */
public class SimpleHttpServer {
	/** 处理Http请求的线程池 */
	private static ThreadPool<HttpRequestHandler> threadPool = new ThreadPoolImpl<>(10);
	/** 资源文件根路径 */
	private static String rootPath;
	/** Server socket and port */
	private static ServerSocket serverSocket;
	private static int port = 8080;

	/**
	 * 设置端口
	 * @param port 端口
	 */
	public static void setPort(int port) {
		if (port <= 0) {
			throw new IllegalArgumentException("port <= 0: " + port);
		}
		SimpleHttpServer.port = port;
	}

	/**
	 * 设置根路径
	 * @param path 根路径
	 */
	public static void setRootPath(String path) {
		if (path != null && path.length() > 0 && new File(path).exists() && new File(path).isDirectory()) {
			SimpleHttpServer.rootPath = path;
		}
	}

	/**
	 * 启动Server
	 * @throws IOException
	 */
	public static void start() throws IOException {
		serverSocket = new ServerSocket(port);
		Socket socket;
		while ((socket = serverSocket.accept()) != null) {
			// 接收客户端socket连接，生成线程池任务，放入线程池执行
			threadPool.execute(new HttpRequestHandler(socket));
		}
		serverSocket.close();
	}

	/**
	 * Http请求处理器
	 */
	private static class HttpRequestHandler implements Runnable {
		private Socket socket;
		private BufferedReader reader;
		private PrintWriter out;

		public HttpRequestHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
				out = new PrintWriter(socket.getOutputStream());
				// 请求行
				String requestLine = reader.readLine();
				// 根据请求资源相对路径，计算资源绝对路径
				String filePath = rootPath + requestLine.split("\\s+")[1];
				// 图片资源
				if (filePath.endsWith("jpg") || filePath.endsWith("ico")) {
					InputStream in = new FileInputStream(filePath);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					int d;
					while ((d = in.read()) != -1) {
						bos.write(d);
					}

					byte[] data = bos.toByteArray();
					out.println("HTTP/1.1 200 OK");
					out.println("Server: Molly");
					out.println("Content-Type: image/jpeg");
					out.println("Content-Length: " + data.length);
					out.println("");
					socket.getOutputStream().write(data, 0, data.length);
					bos.close();
					in.close();
				} else {
					// 文本资源
					BufferedReader br = new BufferedReader(
							new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));

					out.println("HTTP/1.1 200 OK");
					out.println("Server: Molly");
					out.println("Content-Type: text/html;charset=UTF-8");
					out.println("");

					String line;
					while ((line = br.readLine()) != null) {
						out.println(line);
					}
					br.close();
				}
				out.flush();
			} catch (Exception e) {
				out.println("HTTP/1.1 500");
				out.println("");
				out.flush();
			} finally {
				close(reader, out, socket);
			}
		}

	}

	/**
	 * 关闭流或socket
	 */
	private static void close(Closeable... closeables) {
		if (closeables != null) {
			for (Closeable closeable : closeables) {
				try {
					closeable.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
