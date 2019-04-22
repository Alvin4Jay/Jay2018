package JavaNetworkProgramming.chapter8;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 一个基于网络的英文-拉丁语翻译程序
 *
 * @author xuanjian
 */
public class DictClient {
	private static final String SERVER = "dict.org";

	private static final int PORT = 2628;

	private static final int READ_TIMEOUT = 15000;

	public static void main(String[] args) {

		Socket socket = null;
		try {
			socket = new Socket(SERVER, PORT);
			socket.setSoTimeout(READ_TIMEOUT);
			OutputStream out = socket.getOutputStream();
			Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
			writer = new BufferedWriter(writer);

			InputStream in = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
			for (String word : args) {
				define(word, writer, reader);
			}
			writer.write("quit\r\n");
			writer.flush();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException ignored) {

				}
			}
		}
	}

	private static void define(String word, Writer writer, BufferedReader reader) throws IOException {
		writer.write("DEFINE fd-eng-hun " + word + "\r\n");
		writer.flush();

		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			if (line.startsWith("250 ")) {
				return;
			} else if (line.startsWith("552 ")) {
				System.out.println("No definition found for " + word);
				return;
			} else if (line.matches("\\d\\d\\d .*")) {
				continue;
			} else if (line.trim().equals(".")) {
				continue;
			} else {
				System.out.println(line);
			}
		}
	}

}
