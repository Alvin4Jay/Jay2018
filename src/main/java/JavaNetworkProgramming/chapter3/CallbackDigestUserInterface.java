package JavaNetworkProgramming.chapter3;

import javax.xml.bind.DatatypeConverter;

/**
 * @author xuanjian
 */
public class CallbackDigestUserInterface {

	public static void main(String[] args) {
		for (String filename : args) {
			// 计算摘要
			CallbackDigest cb = new CallbackDigest(filename);
			Thread t = new Thread(cb);
			t.start();
		}
	}

	// 回调方法
	public static void receiveDigest(byte[] digest, String name) {
		// 打印
		StringBuilder result = new StringBuilder(name);
		result.append(": ");
		result.append(DatatypeConverter.printHexBinary(digest));
		System.out.println(result);
	}

}
