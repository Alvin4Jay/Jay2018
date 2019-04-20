package JavaNetworkProgramming.chapter7;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 从web网站下载二进制文件并保存到磁盘
 *
 * @author xuanjian
 */
public class BinarySaver {
	public static void main(String[] args) throws IOException {
		for (int i = 0; i < args.length; i++) {
			try {
				URL u = new URL(args[i]);
				saveBinaryFile(u);
			} catch (MalformedURLException e) {
				System.err.println(args[0] + " is not a parseable URL");
			}
		}
	}

	private static void saveBinaryFile(URL u) throws IOException {
		URLConnection uc = u.openConnection();
		String contentType = uc.getContentType();
		int contentLength = uc.getContentLength();

		if (contentType.startsWith("text/") || contentLength == -1) {
			throw new IOException("This is not a binary file.");
		}

		try (InputStream raw = uc.getInputStream()) {
			InputStream in = new BufferedInputStream(raw);
			byte[] data = new byte[contentLength];
			int offset = 0;
			while (offset < contentLength) {
				int bytesRead = in.read(data, offset, data.length - offset);
				if (bytesRead == -1) {
					break;
				}
				offset += bytesRead;
			}

			if (offset != contentLength) {
				throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
			}

			String filename = u.getFile();
			filename = filename.substring(filename.lastIndexOf('/') + 1);
			try (FileOutputStream out = new FileOutputStream(filename)) {
				out.write(data);
				out.flush();
			}
		}
	}
}
