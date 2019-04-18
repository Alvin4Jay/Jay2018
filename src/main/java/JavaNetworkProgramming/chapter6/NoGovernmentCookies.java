package JavaNetworkProgramming.chapter6;

import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;

/**
 * 不接受.gov域的cookie，但接收其他域的cookie
 *
 * @author xuanjian
 */
public class NoGovernmentCookies implements CookiePolicy {
	@Override
	public boolean shouldAccept(URI uri, HttpCookie cookie) {
		if (uri.getAuthority().toLowerCase().endsWith(".gov") || cookie.getDomain().toLowerCase().endsWith(".gov")) {
			return false;
		}
		return true;
	}
}
