package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$4.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ConnectionPool Test
 *
 * @author xuanjian.xuwj
 */
public class ConnectionPoolTest {
	/** 数据库连接池，初始化10个连接 */
	private static ConnectionPool pool = new ConnectionPool(10);
	/** 测试线程启动信号，使得所有Runner线程同时开始获取连接 */
	private static CountDownLatch runnerStart = new CountDownLatch(1);
	/** main线程等待状态返回信号 */
	private static CountDownLatch mainEnd;

	public static void main(String[] args) throws InterruptedException {
		// 测试线程数
		int threadCount = 50;
		mainEnd = new CountDownLatch(threadCount);

		// 创建测试线程并启动
		int count = 20;
		AtomicLong got = new AtomicLong(0);
		AtomicLong notGot = new AtomicLong(0);
		for (int i = 0; i < threadCount; i++) {
			Thread thread = new Thread(new ConnectionRunner(count, got, notGot),
					"ConnectionRunner" + i);
			thread.start();
		}
		// 测试线程同时开始获取连接
		runnerStart.countDown();

		// 等待所有测试线程执行结束，打印获取连接和未获取连接的情况
		mainEnd.await();
		System.out.println("total invoke: " + threadCount * count);
		System.out.println("got connection: " + got.longValue());
		System.out.println("notGot connection: " + notGot.longValue());
	}

	/**
	 * 获取连接的测试线程
	 */
	private static class ConnectionRunner implements Runnable {
		/** 循环获取连接的次数 */
		private int count;
		/** 总获取到的次数 */
		private AtomicLong got;
		/** 总未获取到的次数 */
		private AtomicLong notGot;

		public ConnectionRunner(int count, AtomicLong got, AtomicLong notGot) {
			this.count = count;
			this.got = got;
			this.notGot = notGot;
		}

		@Override
		public void run() {
			try {
				// 当前线程等待，等待runnerStart count为0
				runnerStart.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			while (count > 0) {
				try {
					// 尝试1s内获取连接，获取不到返回null
					Connection connection = pool.fetchConnection(1000);
					// 对获取到和未获取到两种情况进行统计
					if (connection != null) {
						try {
							// 方法调用
							connection.createStatement();
							connection.commit();
						} catch (SQLException e) {
							e.printStackTrace();
						} finally {
							// 释放链接
							pool.releaseConnection(connection);
							got.incrementAndGet();
						}
					} else {
						notGot.incrementAndGet();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					count--;
				}
			}
			// 本线程执行结束，mainEnd count减1
			mainEnd.countDown();
		}
	}

}
