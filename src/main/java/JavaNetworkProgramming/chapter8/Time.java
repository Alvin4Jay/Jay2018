package JavaNetworkProgramming.chapter8;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * 时间协议客户端
 *
 * @author xuanjian
 */
public class Time {

	private static final String HOSTNAME = "time.nist.gov";

	public static void main(String[] args) throws IOException {
		Date date = Time.getDateFromNetwork();

		System.out.println("It is " + date);
	}

	public static Date getDateFromNetwork() throws IOException {
		//		long differenceBetweenEpochs = 2_208_988_800L;
		// 时间协议设定时间起点是1900年，Java Date类起始于1970年，differenceBetweenEpochs表示两者之间的秒差
		TimeZone gmt = TimeZone.getTimeZone("GMT");
		Calendar epoch1900 = Calendar.getInstance(gmt);
		epoch1900.set(1900, 01, 01, 00, 00, 00);
		long epoch1900ms = epoch1900.getTime().getTime();
		Calendar epoch1970 = Calendar.getInstance(gmt);
		epoch1970.set(1970, 01, 01, 00, 00, 00);
		long epoch1970ms = epoch1970.getTime().getTime();
		long differenceInMS = epoch1970ms - epoch1900ms;
		long differenceBetweenEpochs = differenceInMS / 1000;

		Socket socket = null;
		try {
			socket = new Socket(HOSTNAME, 37);
			socket.setSoTimeout(15_000);
			InputStream in = socket.getInputStream();

			long secondsSince1900 = 0;
			for (int i = 0; i < 4; i++) {
				secondsSince1900 = (secondsSince1900 << 8) | in.read(); // 读取字节
			}

			long secondsSince1970 = secondsSince1900 - differenceBetweenEpochs; // s
			long msSince1970 = secondsSince1970 * 1000; // ms

			return new Date(msSince1970);
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ignored) {

				}
			}
		}
	}

}
