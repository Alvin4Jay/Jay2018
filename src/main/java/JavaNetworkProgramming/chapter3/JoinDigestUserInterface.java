package JavaNetworkProgramming.chapter3;

import javax.xml.bind.DatatypeConverter;

/**
 * join Thread
 *
 * @author xuanjian
 */
public class JoinDigestUserInterface {
	public static void main(String[] args) {
		ReturnDigest[] digestThreads = new ReturnDigest[args.length];
		for (int i = 0; i < args.length; i++) {
			// 计算摘要
			digestThreads[i] = new ReturnDigest(args[i]);
			digestThreads[i].start();
		}

		for (int i = 0; i < args.length; i++) {
			try {
				digestThreads[i].join();
				// 打印
				StringBuilder result = new StringBuilder(args[i]);
				result.append(": ");
				byte[] digest = digestThreads[i].getDigest();
				result.append(DatatypeConverter.printHexBinary(digest));
				System.out.println(result);
			} catch (InterruptedException e) {
				System.out.println("Thread Interrupted before completion");
				e.printStackTrace();
			}
		}
	}
}
