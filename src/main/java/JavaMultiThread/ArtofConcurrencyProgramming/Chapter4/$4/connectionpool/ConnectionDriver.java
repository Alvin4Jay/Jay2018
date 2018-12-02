package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$4.connectionpool;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;

/**
 * Connection Proxy创建
 *
 * @author xuanjian.xuwj
 */
public class ConnectionDriver {

	/**
	 * 创建Connection的动态代理
	 * @return Connection Proxy
	 */
	public static Connection createConnection() {
		return (Connection) Proxy
				.newProxyInstance(ConnectionDriver.class.getClassLoader(), new Class[]{Connection.class},
						new ConnectionHandler());
	}

	/**
	 * Connection Proxy处理器
	 */
	private static class ConnectionHandler implements InvocationHandler {
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// commit()方法调用时，睡眠100ms
			if ("commit".equals(method.getName())) {
				Thread.sleep(100);
			}
			return null;
		}
	}

}
