package JavaNetworkProgramming.chapter3;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 回调通知
 *
 * @author xuanjian
 */
public class CallbackDigest implements Runnable {

	private String filename;

	public CallbackDigest(String filename) {
		this.filename = filename;
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
			// 回调
			CallbackDigestUserInterface.receiveDigest(digest, filename);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}
}
