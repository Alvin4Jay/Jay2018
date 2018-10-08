package javabasiscode.NIO;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Jay on 2017/9/20
 */
public class Demo01 {
    public static void main(String[] args) {
        try {
            RandomAccessFile raf = new RandomAccessFile("d:/upload/test.txt", "rw");
            FileChannel channel = raf.getChannel(); //获取通道
            ByteBuffer buffer = ByteBuffer.allocate(48); //分配Buffer

            int byteRead = channel.read(buffer);

            while(byteRead != -1){
                System.out.println("Read" + byteRead);
                buffer.flip();  //从写缓冲切换到读缓冲模式
                while(buffer.hasRemaining()){
                    System.out.print((char)buffer.get());
                }
                buffer.clear(); //清空之前已读的
                byteRead = channel.read(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
