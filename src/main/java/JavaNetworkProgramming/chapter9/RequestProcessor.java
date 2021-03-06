package JavaNetworkProgramming.chapter9;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 处理HTTP请求的Runnable类
 *
 * @author xuanjian
 */
public class RequestProcessor implements Runnable {

	private final static Logger logger = Logger.getLogger(RequestProcessor.class.getCanonicalName());
	private File rootDirectory;
	private String indexFileName = "index.html";
	private Socket connection;

	public RequestProcessor(File rootDirectory, String indexFileName, Socket connection) {
		if (rootDirectory.isFile()) {
			throw new IllegalArgumentException("rootDirectory must be a directory, not a file");
		}
		try {
			rootDirectory = rootDirectory.getCanonicalFile();
		} catch (IOException ex) {
		}
		this.rootDirectory = rootDirectory;
		if (indexFileName != null) {
			this.indexFileName = indexFileName;
		}
		this.connection = connection;
	}

	@Override
	public void run() {
		// for security checks
		String root = rootDirectory.getPath();
		try {
			OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
			Writer out = new OutputStreamWriter(raw);
			Reader in = new InputStreamReader(new BufferedInputStream(connection.getInputStream()),
					StandardCharsets.US_ASCII);
			StringBuilder requestLine = new StringBuilder();
			while (true) {
				int c = in.read();
				if (c == '\r' || c == '\n') {
					break;
				}
				requestLine.append((char) c);
			}
			String get = requestLine.toString();

			logger.info(connection.getRemoteSocketAddress() + " " + get);
			String[] tokens = get.split("\\s+");
			String method = tokens[0];
			String version = "";
			if ("GET".equals(method)) {
				String fileName = tokens[1];
				if (fileName.endsWith("/")) {
					fileName += indexFileName;
				}
				String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
				if (tokens.length > 2) {
					version = tokens[2];
				}
				File theFile = new File(rootDirectory, fileName.substring(1));
				if (theFile.canRead() && theFile.getCanonicalPath().startsWith(root)) {
					// Don't let clients outside the document root && theFile.getCanonicalPath().startsWith(root)) {
					byte[] theData = Files.readAllBytes(theFile.toPath());
					// send a MIME header
					if (version.startsWith("HTTP/")) {
						sendHeader(out, "HTTP/1.0 200 OK", contentType, theData.length);
					}
					// send the file; it may be an image or other binary data // so use the underlying output stream
					// instead of the writer
					raw.write(theData);
					raw.flush();
				} else { // can't find the file
					String body = "<HTML>\r\n" + "<HEAD><TITLE>File Not Found</TITLE>\r\n" + "</HEAD>\r\n" + "<BODY>"
							+ "<H1>HTTP Error 404: File Not Found</H1>\r\n" + "</BODY></HTML>\r\n";
					// send a MIME header
					if (version.startsWith("HTTP/")) {
						sendHeader(out, "HTTP/1.0 404 File Not Found", "text/html; charset=utf-8", body.length());
					}
					out.write(body);
					out.flush();
				}
			} else { // method does not equal "GET"
				String body = "<HTML>\r\n" + "<HEAD><TITLE>Not Implemented</TITLE>\r\n" + "</HEAD>\r\n" + "<BODY>"
						+ "<H1>HTTP Error 501: Not Implemented</H1>\r\n" + "</BODY></HTML>\r\n";
				// send a MIME header
				if (version.startsWith("HTTP/")) {
					sendHeader(out, "HTTP/1.0 501 Not Implemented", "text/html; charset=utf-8", body.length());
				}
				out.write(body);
				out.flush();
			}
		} catch (IOException ex) {
			logger.log(Level.WARNING, "Error talking to " + connection.getRemoteSocketAddress(), ex);
		} finally {
			try {
				connection.close();
			} catch (IOException ignored) {}
		}
	}

	private void sendHeader(Writer out, String responseCode, String contentType, int length) throws IOException {
		out.write(responseCode + "\r\n");
		Date now = new Date();
		out.write("Date: " + now + "\r\n");
		out.write("Server: JHTTP 2.0\r\n");
		out.write("Content-length: " + length + "\r\n");
		out.write("Content-type: " + contentType + "\r\n\r\n");
		out.flush();
	}

}
