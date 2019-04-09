package JavaNetworkProgramming.chapter3;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 实例回调
 *
 * @author xuanjian
 */
public class InstanceCallbackDigest implements Runnable {

	private String filename;

	private InstanceCallbackDigestUserInterface callback;

	public InstanceCallbackDigest(String filename, InstanceCallbackDigestUserInterface callback) {
		this.filename = filename;
		this.callback = callback;
	}

	@Override
	public void run() {
		try {
			FileInputStream in = new FileInputStream(filename);
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			DigestInputStream din = new DigestInputStream(in, md);

			while (din.read() != -1)
				;
			din.close();
			byte[] digest = md.digest();
			callback.receiveDigest(digest);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}
}
