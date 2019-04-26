package JavaNetworkProgramming.chapter11;

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
 * 一个非阻塞的chargen服务器
 *
 * @author xuanjian
 */
public class ChargenServer {

	private static final int DEFAULT_PORT = 19;

	public static void main(String[] args) {

		int port;
		try {
			port = Integer.parseInt(args[0]);
		} catch (RuntimeException e) {
			port = DEFAULT_PORT;
		}
		System.out.println("Listening for connections on port " + port);

		byte[] rotation = new byte[95 * 2];
		for (byte i = ' '; i < '~'; i++) {
			rotation[i - ' '] = i;
			rotation[i + 95 - ' '] = i;
		}

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
			e.printStackTrace();
			return;
		}

		while (true) {
			try {
				selector.select();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			while (iterator.hasNext()) {
				SelectionKey key = iterator.next();
				iterator.remove(); // 移除key，重要
				try {
					if (key.isAcceptable()) {
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						SocketChannel client = server.accept();
						System.out.println("Accepted connection from " + client);
						client.configureBlocking(false);
						SelectionKey sk = client.register(selector, SelectionKey.OP_WRITE);

						ByteBuffer buffer = ByteBuffer.allocate(74);
						buffer.put(rotation, 0, 72);
						buffer.put((byte) '\r');
						buffer.put((byte) '\n');
						buffer.flip();
						sk.attach(buffer);
					} else if (key.isWritable()) {
						SocketChannel client = (SocketChannel) key.channel();
						ByteBuffer buffer = (ByteBuffer) key.attachment();
						if (!buffer.hasRemaining()) {
							// 准备下一行新数据
							buffer.rewind();
							byte first = buffer.get();
							buffer.rewind();
							int position = first - ' ' + 1;
							buffer.put(rotation, position, 72);
							buffer.put((byte) '\r');
							buffer.put((byte) '\n');
							buffer.flip();
						}
						client.write(buffer);
					}
				} catch (IOException e) {
					key.channel();
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
