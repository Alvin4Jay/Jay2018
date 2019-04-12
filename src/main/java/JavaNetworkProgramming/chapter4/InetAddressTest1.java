package JavaNetworkProgramming.chapter4;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 创建InetAddress对象
 *
 * @author xuanjian
 */
public class InetAddressTest1 {
	public static void main(String[] args) {
		try {
			// 正向解析，与DNS交互
			InetAddress address = InetAddress.getByName("www.oreilly.com");
			System.out.println(address);

			System.out.println("-------------");
			// 反向解析，与DNS交互
			InetAddress address2 = InetAddress.getByName("121.51.36.46");
			System.out.println(address2.getHostName());

			// 获取主机名相关的所有IP，与DNS交互
			System.out.println("-------------");
			InetAddress[] addresses = InetAddress.getAllByName("www.qq.com");
			for (InetAddress inetAddress : addresses) {
				System.out.println(inetAddress);
			}

			// 获取本机IP，与DNS交互
			System.out.println("-------------");
			InetAddress localhost = InetAddress.getLocalHost();
			System.out.println(localhost);

			System.out.println("--------------");
			// 不保证主机一定存在或者主机名能正确映射到IP地址，不与DNS交互
			byte[] bts = {107, 23, (byte) 216, (byte) 196};
			InetAddress lessWrong = InetAddress.getByAddress(bts);
			InetAddress lessWrongWithName = InetAddress.getByAddress("lesswrong.com", bts);
			System.out.println(lessWrong);
			System.out.println(lessWrongWithName);
		} catch (UnknownHostException e) {
			System.out.println("Could not found www.oreilly.com");
		}
	}
}
