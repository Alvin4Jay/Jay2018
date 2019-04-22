package JavaNetworkProgramming.chapter8;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 通过与time.nist.gov对话构造一个Date
 *
 * @author xuanjian
 */
public class Daytime {

	public static void main(String[] args) throws IOException, ParseException {
		System.out.println("It is " + getDateFromNetwork().toString());
	}

	public static Date getDateFromNetwork() throws IOException, ParseException {
		try (Socket socket = new Socket("time.nist.gov", 13)) {
			socket.setSoTimeout(15_000);
			InputStream in = socket.getInputStream();
			InputStreamReader reader = new InputStreamReader(in, StandardCharsets.US_ASCII);
			StringBuilder time = new StringBuilder();
			for (int c = reader.read(); c != -1; c = reader.read()) {
				time.append((char) c);
			}
			return parseDate(time.toString());
		}
	}

	private static Date parseDate(String s) throws ParseException {
		String[] pieces = s.split(" ");
		String daytime = pieces[1] + " " + pieces[2] + " UTC";
		DateFormat format = new SimpleDateFormat("yy-MM-dd hh:mm:ss z");
		return format.parse(daytime);
	}

}
