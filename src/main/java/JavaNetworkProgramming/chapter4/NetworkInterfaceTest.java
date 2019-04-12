package JavaNetworkProgramming.chapter4;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * NetworkInterface测试
 *
 * @author xuanjian
 */
public class NetworkInterfaceTest {
	public static void main(String[] args) {
		test03();
		//		test02();
		//		test01();
	}

	// 获取本机所有NetworkInterface对象
	private static void test03() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface ni = interfaces.nextElement();
				System.out.println(ni);
				Enumeration<InetAddress> ias = ni.getInetAddresses();
				while (ias.hasMoreElements()) {
					System.out.println("-----" + ias.nextElement().getHostAddress());
				}
			}
		} catch (SocketException e) {
			System.err.println("Could not list network interfaces.");
		}
	}

	// 根据绑定的ip获取NetworkInterface对象
	private static void test02() {
		try {
			InetAddress local = InetAddress.getByName("127.0.0.1");
			NetworkInterface ni = NetworkInterface.getByInetAddress(local);
			if (ni == null) {
				System.err.println("That's weird. No local loopback address.");
			} else {
				System.out.println(ni);
			}
		} catch (SocketException e) {
			System.err.println("Could not list network interfaces.");
		} catch (UnknownHostException e) {
			System.err.println("Could not lookup 127.0.0.1");
		}
	}

	// 根据名称获取NetworkInterface对象
	private static void test01() {
		try {
			NetworkInterface ni = NetworkInterface.getByName("en0");
			if (ni == null) {
				System.err.println("No such interface: en0");
			} else {
				System.out.println(ni);
			}
		} catch (SocketException e) {
			System.err.println("Could not list sockets.");
		}
	}
}
