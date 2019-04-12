package JavaNetworkProgramming.chapter4;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * InetAddress 确定IP地址是IPv4还是IPv6
 *
 * @author xuanjian
 */
public class InetAddressTest3 {
	public static void main(String[] args) {
		try {
			InetAddress machine = InetAddress.getLocalHost();
			System.out.println(getVersion(machine)); // 4
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private static int getVersion(InetAddress ia) {
		byte[] address = ia.getAddress();
		if (address.length == 4) {
			return 4;
		} else if (address.length == 16) {
			return 6;
		} else {
			return -1;
		}
	}
}
