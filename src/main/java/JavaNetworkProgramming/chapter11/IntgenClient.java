package JavaNetworkProgramming.chapter11;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

/**
 * Intgen客户端
 *
 * @author xuanjian
 */
public class IntgenClient {

	private static final int DEFAULT_PORT = 1919;

	public static void main(String[] args) {

		if (args.length == 0) {
			System.out.println("Usage: java IntgenClient host [port]");
			return;
		}

		int port;
		try {
			port = Integer.parseInt(args[1]);
		} catch (RuntimeException e) {
			port = DEFAULT_PORT;
		}

		try {
			SocketAddress address = new InetSocketAddress(args[0], port);
			SocketChannel client = SocketChannel.open(address);

			ByteBuffer buffer = ByteBuffer.allocate(4);
			IntBuffer view = buffer.asIntBuffer();

			for (int expected = 0; ; expected++) {
				client.read(buffer);
				int actual = view.get();
				buffer.clear(); // 清空，buffer准备写入
				view.rewind();  // pos=0，准备读取

				if (expected != actual) {
					System.out.println("Expected: " + expected + ", actual: " + actual);
					break;
				}
				System.out.println(actual);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
