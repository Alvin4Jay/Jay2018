package JavaNetworkProgramming.chapter4;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * InetAddress getter method
 *
 * @author xuanjian
 */
public class InetAddressTest2 {
	public static void main(String[] args) {
		try {
			InetAddress machine = InetAddress.getLocalHost();
			System.out.println(machine.getHostName());
			System.out.println(machine.getCanonicalHostName());

			String address = machine.getHostAddress();
			System.out.println("My address is: " + address);

			InetAddress ia = InetAddress.getByName("121.51.36.46");
			System.out.println(ia.getCanonicalHostName());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
