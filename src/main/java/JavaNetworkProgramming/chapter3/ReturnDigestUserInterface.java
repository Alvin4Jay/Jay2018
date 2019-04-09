package JavaNetworkProgramming.chapter3;

import javax.xml.bind.DatatypeConverter;

/**
 * 使用getter获取线程输出的主程序
 *
 * @author xuanjian
 */
public class ReturnDigestUserInterface {
	public static void main(String[] args) {
		for (String filename : args) {
			// 计算摘要
			ReturnDigest dr = new ReturnDigest(filename);
			dr.start();

			// 打印
			StringBuilder result = new StringBuilder(filename);
			result.append(": ");
			byte[] digest = dr.getDigest();
			result.append(DatatypeConverter.printHexBinary(digest));
			System.out.println(result);
		}
	}
}
