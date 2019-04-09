package JavaNetworkProgramming.chapter3;

import javax.xml.bind.DatatypeConverter;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 实现Runnable接口
 *
 * @author xuanjian
 */
public class DigestRunnable implements Runnable {

	private String filename;

	public DigestRunnable(String filename) {
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

			StringBuilder result = new StringBuilder(filename);
			result.append(": ");
			result.append(DatatypeConverter.printHexBinary(digest));
			System.out.println(result);
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		for (String filename : args) {
			DigestRunnable digestRunnable = new DigestRunnable(filename);
			Thread t = new Thread(digestRunnable);
			t.start();
		}
	}
}
