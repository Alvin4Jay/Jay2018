package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$3;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

/**
 * 管道流 实现线程间通讯
 *
 * @author xuanjian.xuwj
 */
public class Piped {
	public static void main(String[] args) throws IOException {
		PipedWriter writer = new PipedWriter();
		PipedReader reader = new PipedReader();
		// 连接输出流和输入流
		writer.connect(reader);

		Thread printThread = new Thread(new Print(reader), "PrintThread");
		printThread.start();

		int value;
		try {
			while ((value = System.in.read()) != -1) {
				writer.write(value);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}

	}

	private static class Print implements Runnable {
		private PipedReader reader;

		public Print(PipedReader reader) {
			this.reader = reader;
		}

		@Override
		public void run() {
			int value;
			try {
				while ((value = reader.read()) != -1) {
					System.out.print((char) value);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
