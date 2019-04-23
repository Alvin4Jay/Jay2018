package JavaNetworkProgramming.chapter9;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * 时间服务器
 *
 * @author xuanjian
 */
public class TimeServer {

	private static final int PORT = 37;

	public static void main(String[] args) {
		// 时间协议设定时间起点是1900年，Java Date类起始于1970年，differenceBetweenEpochs表示两者之间的秒差
		long differenceBetweenEpochs = 2_208_988_800L; // 秒s

		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			while (true) {
				try (Socket socket = serverSocket.accept()) {
					OutputStream out = socket.getOutputStream();
					Date now = new Date();
					long msSince1970 = now.getTime();
					long secondsSince1970 = msSince1970 / 1000;
					long secondsSince1900 = secondsSince1970 + differenceBetweenEpochs;
					byte[] time = new byte[4];
					time[0] = (byte) ((secondsSince1900 & 0x00000000FF000000L) >> 24);
					time[1] = (byte) ((secondsSince1900 & 0x0000000000FF0000L) >> 16);
					time[2] = (byte) ((secondsSince1900 & 0x000000000000FF00L) >> 8);
					time[3] = (byte) (secondsSince1900 & 0x00000000000000FFL);
					out.write(time);
					out.flush();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

}
