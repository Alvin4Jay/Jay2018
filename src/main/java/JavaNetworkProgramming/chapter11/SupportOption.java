package JavaNetworkProgramming.chapter11;

import java.io.IOException;
import java.net.SocketOption;
import java.nio.channels.*;

/**
 * 列出支持的选择
 *
 * @author xuanjian
 */
public class SupportOption {
	public static void main(String[] args) throws IOException {
		printOptions(SocketChannel.open());
		printOptions(ServerSocketChannel.open());
		printOptions(AsynchronousSocketChannel.open());
		printOptions(AsynchronousServerSocketChannel.open());
		printOptions(DatagramChannel.open());
	}

	private static void printOptions(NetworkChannel channel) throws IOException {
		System.out.println(channel.getClass().getSimpleName() + " support: ");
		try {
			for (SocketOption<?> option : channel.supportedOptions()) {
				System.out.println(option.name() + ": " + channel.getOption(option));
			}
		} catch (Throwable e) {
			System.err.println(e.getMessage());
		}
		System.out.println();
		channel.close();
	}
}
