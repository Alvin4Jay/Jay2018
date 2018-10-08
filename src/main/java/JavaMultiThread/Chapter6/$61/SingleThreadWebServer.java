package JavaMultiThread.Chapter6.$61;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *  串行Web服务器
 */
public class SingleThreadWebServer {
    public static void main(String[] args) throws IOException {
        ServerSocket socket = new ServerSocket(80); //监听端口
        while(true){
            Socket connection = socket.accept(); //接收请求
            handleRequest(connection); //处理请求
        }
    }

    private static void handleRequest(Socket connection) {
    }
}
