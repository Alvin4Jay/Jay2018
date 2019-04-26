package JavaNetworkProgramming.chapter11.asynchrous;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * AsynchronousSocketChannel Future
 *
 * @author xuanjian
 */
public class AsynchronousSocketChannelTest1 {
	public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
		SocketAddress address = new InetSocketAddress(args[0], 19);

		AsynchronousSocketChannel client = AsynchronousSocketChannel.open();

		Future<Void> connected = client.connect(address);

		ByteBuffer buffer = ByteBuffer.allocate(74);
		// 等待连接完成
		connected.get();

		// 从连接读取数据
		Future<Integer> future = client.read(buffer);

		// 做其他工作

		// 等待读取完成
		future.get();

		// 读取数据并输出
		buffer.flip();
		WritableByteChannel out = Channels.newChannel(System.out);
		out.write(buffer);
	}
}
