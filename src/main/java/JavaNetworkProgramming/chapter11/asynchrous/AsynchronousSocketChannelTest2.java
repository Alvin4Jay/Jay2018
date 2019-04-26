package JavaNetworkProgramming.chapter11.asynchrous;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * AsynchronousSocketChannel CompletionHandler
 *
 * @author xuanjian
 */
public class AsynchronousSocketChannelTest2 {
	public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
		SocketAddress address = new InetSocketAddress(args[0], 19);

		AsynchronousSocketChannel client = AsynchronousSocketChannel.open();

		Future<Void> connected = client.connect(address);

		ByteBuffer buffer = ByteBuffer.allocate(74);

		// 等待连接完成
		connected.get();

		CompletionHandler<Integer, ByteBuffer> handler = new LineHandler();
		// 从连接读取数据
		client.read(buffer, buffer, handler);

		// 做其他工作
	}
}
