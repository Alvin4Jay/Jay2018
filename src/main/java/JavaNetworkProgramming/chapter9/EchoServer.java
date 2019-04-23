package JavaNetworkProgramming.chapter9;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * echo服务器 NIO
 *
 * @author xuanjian
 */
public class EchoServer {

	private static final int DEFAULT_PORT = 7;

	public static void main(String[] args) {

		int port;

		try {
			port = Integer.parseInt(args[0]);
		} catch (RuntimeException e) {
			port = DEFAULT_PORT;
		}
		System.out.println("Listening for connections on port " + port);

		ServerSocketChannel serverSocketChannel;
		Selector selector;

		try {
			serverSocketChannel = ServerSocketChannel.open();
			ServerSocket ss = serverSocketChannel.socket();
			ss.bind(new InetSocketAddress(port));
			serverSocketChannel.configureBlocking(false);
			selector = Selector.open();
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}

		while (true) {
			try {
				selector.select();
			} catch (IOException e) {
				System.err.println(e.getMessage());
				break;
			}

			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove();
				try {
					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						System.out.println("Accepted connection from " + client);
						client.configureBlocking(false);
						SelectionKey clientKey = client
								.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
						ByteBuffer attachment = ByteBuffer.allocate(100);
						clientKey.attach(attachment);
					}
					if (key.isReadable()) {
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer output = (ByteBuffer) key.attachment();
						client.read(output);  // 读取数据
					}
					if (key.isWritable()) {
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer output = (ByteBuffer) key.attachment();
						output.flip();
						client.write(output);
						output.compact();
					}
				} catch (IOException e) {
					e.printStackTrace();
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

}
