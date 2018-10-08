package javabasiscode.NIO;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * Created by Jay on 2017/9/20
 */
public class Demo02 {
    public static void main(String[] args) {
        try {
            //通道之间传输数据
            RandomAccessFile fromFile = new RandomAccessFile("d:/upload/08ce711cc19c4ecf8cf79286ee2a56ce.jpg", "rw");
            FileChannel fromChannel = fromFile.getChannel();

            RandomAccessFile toFile = new RandomAccessFile("d:/upload/to.jpg", "rw");
            FileChannel toChannel = toFile.getChannel();

            long position = 0;
            long count = fromChannel.size();

            toChannel.transferFrom(fromChannel, position, count);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
