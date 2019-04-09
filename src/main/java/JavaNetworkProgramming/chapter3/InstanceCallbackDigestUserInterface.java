package JavaNetworkProgramming.chapter3;

import javax.xml.bind.DatatypeConverter;

/**
 * @author xuanjian
 */
public class InstanceCallbackDigestUserInterface {
	private String filename;
	private byte[] digest;

	public InstanceCallbackDigestUserInterface(String filename) {
		this.filename = filename;
	}

	public void calculateDigest() {
		InstanceCallbackDigest cb = new InstanceCallbackDigest(filename, this);
		Thread t = new Thread(cb);
		t.start();
	}

	// 回调方法
	void receiveDigest(byte[] digest) {
		this.digest = digest;
		System.out.println(this);
	}

	@Override
	public String toString() {
		String result = filename + ": ";
		if (digest != null) {
			result += DatatypeConverter.printHexBinary(digest);
		} else {
			result += "digest not available";
		}
		return result;
	}

	public static void main(String[] args) {
		for (String filename : args) {
			InstanceCallbackDigestUserInterface d = new InstanceCallbackDigestUserInterface(filename);
			d.calculateDigest();
		}
	}
}
