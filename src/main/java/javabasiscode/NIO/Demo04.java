package javabasiscode.NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

/**
 * Created by Jay on 2017/9/20
 */
public class Demo04 {
    public static void main(String[] args) {
        try {
            SocketChannel channel = SocketChannel.open();

            channel.connect(new InetSocketAddress("http://jenkov.com", 80));


        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
