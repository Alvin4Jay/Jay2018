package JavaNetworkProgramming.chapter8;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * whois类
 *
 * @author xuanjian
 */
public class Whois {

	private static final int DEFAULT_PORT = 43;

	private static final String DEFAULT_HOST = "whois.internic.net";

	private int port;

	private InetAddress host;

	public Whois(InetAddress host, int port) {
		this.host = host;
		this.port = port;
	}

	public Whois(InetAddress host) {
		this(host, DEFAULT_PORT);
	}

	public Whois(String host, int port) throws UnknownHostException {
		this(InetAddress.getByName(host), port);
	}

	public Whois(String host) throws UnknownHostException {
		this(InetAddress.getByName(host), DEFAULT_PORT);
	}

	public Whois() throws UnknownHostException {
		this(DEFAULT_HOST, DEFAULT_PORT);
	}

	public enum SearchFor {
		ANY("Any"), NETWORK("Network"), PERSON("Person"), HOST("Host"), DOMAIN("Domain"), ORGANIZATION(
				"Organization"), GROUP("Group"), GATEWAY("Gateway"), ASN("ASN");

		private String label;

		SearchFor(String label) {
			this.label = label;
		}
	}

	public enum SearchIn {
		ALL(""), NAME("Name"), MAILBOX("Mailbox"), HANDLE("!");

		private String label;

		SearchIn(String label) {
			this.label = label;
		}
	}

	public String lookupNames(String target, SearchFor category, SearchIn group, boolean exactMatch)
			throws IOException {
		String suffix = "";
		if (!exactMatch) {
			suffix = ".";
		}

		String prefix = category.label + " " + group.label;
		String query = prefix + target + suffix;

		Socket socket = new Socket();

		try {
			SocketAddress socketAddress = new InetSocketAddress(host, port);
			socket.connect(socketAddress);

			Writer out = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
			out.write(query + "\r\n");
			out.flush();

			StringBuilder response = new StringBuilder();
			String theLine;
			while ((theLine = in.readLine()) != null) {
				response.append(theLine);
				response.append("\r\n");
			}
			return response.toString();
		} finally {
			socket.close();
		}
	}

}
