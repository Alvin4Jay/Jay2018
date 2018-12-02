package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$4.connectionpool;

import java.sql.Connection;
import java.util.LinkedList;

/**
 * Connection Pool 模拟的数据库连接池
 *
 * @author xuanjian.xuwj
 */
public class ConnectionPool {
	/** 放置Connection */
	private final LinkedList<Connection> pool = new LinkedList<>();

	/**
	 * 初始化
	 * @param poolSize 池大小
	 */
	public ConnectionPool(int poolSize) {
		if (poolSize <= 0) {
			throw new IllegalArgumentException("poolSize <= 0: " + poolSize);
		}
		for (int i = 0; i < poolSize; i++) {
			pool.addLast(ConnectionDriver.createConnection());
		}
	}

	/**
	 * 获取数据库连接
	 * @param millis 最长等待时间，超出返回null
	 * @return Connection
	 * @throws InterruptedException 中断
	 */
	public Connection fetchConnection(long millis) throws InterruptedException {
		synchronized (pool) {
			// millis<=0，认为是完全超时模式
			if (millis <= 0) {
				while (pool.isEmpty()) {
					pool.wait();
				}
				return pool.removeFirst();
			} else {
				// 等待超时模式
				long future = System.currentTimeMillis() + millis;
				long remaining = millis;
				while (pool.isEmpty() && remaining > 0) {
					pool.wait(remaining);
					remaining = future - System.currentTimeMillis();
				}

				Connection connection = null;
				if (!pool.isEmpty()) {
					connection = pool.removeFirst();
				}
				return connection;
			}
		}
	}

	/**
	 * 释放链接
	 * @param connection 连接
	 */
	public void releaseConnection(Connection connection) {
		if (connection != null) {
			synchronized (pool) {
				// 释放连接
				pool.addLast(connection);
				// 通知所有消费者
				pool.notifyAll();
			}
		}
	}

}
