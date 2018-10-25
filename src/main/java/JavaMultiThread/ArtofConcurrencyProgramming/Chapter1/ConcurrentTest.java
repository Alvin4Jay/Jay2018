package JavaMultiThread.ArtofConcurrencyProgramming.Chapter1;

/**
 * 测试串行与并行执行的快慢
 *
 * @author xuanjian.xuwj
 * @date 2018/10/24 下午7:14
 */
public class ConcurrentTest {

	private static final long COUNT = 1000000000L;

	public static void main(String[] args) throws InterruptedException {
	    concurrent();
	    serial();
	}


	private static void concurrent() throws InterruptedException {
		long start = System.currentTimeMillis();

		Thread thread = new Thread(() -> {
			int a = 0;
			for (long i = 0; i < COUNT; i++) {
				a += 5;
			}
			System.out.println(a);
		});
		thread.start();
		int b = 0;
		for (long i = 0; i < COUNT; i++) {
			b --;
		}
		// main线程等待thread线程执行结束
		thread.join();

		long time = System.currentTimeMillis() - start;
		System.out.println("concurrent: " + time + "ms, b=" + b);
	}

	private static void serial() {
		long start = System.currentTimeMillis();

		int a = 0;
		for (long i = 0; i < COUNT; i++) {
			a += 5;
		}
		int b = 0;
		for (long i = 0; i < COUNT; i++) {
			b --;
		}

		long time = System.currentTimeMillis() - start;
		System.out.println("serial: " + time + "ms, a=" + a + ",b=" + b);
	}

}
