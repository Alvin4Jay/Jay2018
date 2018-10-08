package javabasiscode.NIO;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

/**
 * Created by Jay on 2017/9/20
 */
public class Demo05 {
    public static void main(String[] args) {
        try {
            Pipe pipe = Pipe.open();

            Pipe.SinkChannel sinkChannel = pipe.sink();

            String newData = "New String to write to file..." + System.currentTimeMillis();
            System.out.println(newData.getBytes().length);
            ByteBuffer buf = ByteBuffer.allocate(48);
            buf.clear();
            buf.put(newData.getBytes());

            buf.flip();

            while(buf.hasRemaining()) {
                sinkChannel.write(buf);
            }

            Pipe.SourceChannel sourceChannel = pipe.source();

            buf = ByteBuffer.allocate(48);

            int bytesRead = sourceChannel.read(buf);

            System.out.println("read " + bytesRead);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
