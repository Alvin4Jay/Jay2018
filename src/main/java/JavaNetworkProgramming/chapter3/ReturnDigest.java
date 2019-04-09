package JavaNetworkProgramming.chapter3;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 使用getter方法返回结果
 *
 * @author xuanjian
 */
public class ReturnDigest extends Thread {
	private String filename;
	private byte[] digest;

	public ReturnDigest(String filename) {
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

			this.digest = md.digest();
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getDigest() {
		return digest;
	}
}
