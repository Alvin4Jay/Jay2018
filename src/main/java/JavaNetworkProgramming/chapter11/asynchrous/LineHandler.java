package JavaNetworkProgramming.chapter11.asynchrous;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.CompletionHandler;
import java.nio.channels.WritableByteChannel;

/**
 * CompletionHandler
 *
 * @author xuanjian
 */
public class LineHandler implements CompletionHandler<Integer, ByteBuffer> {

	@Override
	public void completed(Integer result, ByteBuffer attachment) {
		attachment.flip();
		WritableByteChannel out = Channels.newChannel(System.out);
		try {
			out.write(attachment);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		System.err.println(exc.getMessage());
	}

}
