package JavaNetworkProgramming.chapter11;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

/**
 * 一个提供文件的非阻塞式HTTP服务器 Bytebuffer.duplicate()
 *
 * @author xuanjian
 */
public class NonblockingSingleFileHTTPServer {

	private ByteBuffer contentBuffer;

	private int port;

	public NonblockingSingleFileHTTPServer(ByteBuffer data, String encoding, String mimeType, int port) {
		this.port = port;
		String header =
				"HTTP/1.0 200 OK\r\n" + "Server: NonblockingSingleFileHTTPServer\r\n" + "Content-length: " + data
						.limit() + "\r\n" + "Content-type: " + mimeType + "\r\n\r\n";
		byte[] headerData = header.getBytes(StandardCharsets.US_ASCII);

		ByteBuffer buffer = ByteBuffer.allocate(headerData.length + data.limit());
		buffer.put(headerData);
		buffer.put(data);
		buffer.flip();
		this.contentBuffer = buffer;
	}

	public void run() throws IOException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		ServerSocket serverSocket = serverChannel.socket();
		Selector selector = Selector.open();
		InetSocketAddress localPort = new InetSocketAddress(port);
		serverSocket.bind(localPort);
		serverChannel.configureBlocking(false);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		while (true) {
			selector.select();

			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while (keys.hasNext()) {
				SelectionKey key = keys.next();
				keys.remove();
				try {
					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ);
					} else if (key.isReadable()) {
						// 不用费力地解析HTTP首部
						// 只需读取
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(4096);
						client.read(buffer);
						// 将通道切换为只写模式
						key.interestOps(SelectionKey.OP_WRITE);
						key.attach(contentBuffer.duplicate()); // 添加副本
					} else if (key.isWritable()) {
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						if (buffer.hasRemaining()) {
							client.write(buffer);
						} else {
							client.close();
						}
					}
				} catch (IOException e) {
					key.cancel();
					try {
						key.channel().close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: java NonblockingSingleFileHTTPServer file port encoding");
			return;
		}

		try {
			// read the single file to serve String contentType =
			String contentType = URLConnection.getFileNameMap().getContentTypeFor(args[0]);
			Path file = FileSystems.getDefault().getPath(args[0]);
			byte[] data = Files.readAllBytes(file);
			ByteBuffer input = ByteBuffer.wrap(data);
			// set the port to listen on
			int port;
			try {
				port = Integer.parseInt(args[1]);
				if (port < 1 || port > 65535) {
					port = 80;
				}
			} catch (RuntimeException ex) {
				port = 80;
			}
			String encoding = "UTF-8";
			if (args.length > 2) {
				encoding = args[2];
			}
			NonblockingSingleFileHTTPServer server = new NonblockingSingleFileHTTPServer(input, encoding, contentType,
					port);
			server.run();
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}

}
