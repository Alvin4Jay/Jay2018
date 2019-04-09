package JavaNetworkProgramming.chapter3;

import java.io.*;
import java.util.Date;

/**
 * 日志文件
 *
 * @author xuanjian
 */
public class LogFile {
	private Writer out;

	public LogFile(File file) throws IOException {
		FileWriter fw = new FileWriter(file);
		this.out = new BufferedWriter(fw);
	}

	public void writeEntry(String message) throws IOException {
		// 同步代码块
		synchronized (out) {
			Date d = new Date();
			out.write(d.toString());
			out.write('\t');
			out.write(message);
			out.write("\r\n");
		}
	}

	public void close() throws IOException {
		out.flush();
		out.close();
	}
}
