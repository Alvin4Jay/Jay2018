package JavaMultiThread.ArtofConcurrencyProgramming.Chapter1;

import java.util.concurrent.TimeUnit;

/**
 * Dead Lock 死锁
 *
 * @author xuanjian.xuwj
 * @date 2018/10/24 下午7:26
 */
public class DeadLockDemo {
	private static String A = "A";
	private static String B = "B";

	public static void main(String[] args) {
		new DeadLockDemo().deadLock();
	}

	private void deadLock() {

		Thread t1 = new Thread(() -> {
			synchronized (A) {
				try {
					// 让出CPU，不释放锁
					TimeUnit.SECONDS.sleep(4);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				synchronized (B) {
					System.out.println("1");
				}
			}
		});
		Thread t2 = new Thread(() -> {
			synchronized (B) {
				synchronized (A) {
					System.out.println("2");
				}
			}
		});
		t1.start();
		t2.start();

	}
}
