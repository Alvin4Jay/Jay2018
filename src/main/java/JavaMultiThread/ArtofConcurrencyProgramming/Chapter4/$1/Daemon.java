package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1;

/**
 * Daemon线程
 *
 * @author xuanjian.xuwj
 */
public class Daemon {

	public static void main(String[] args){
	    Thread thread = new Thread(new DaemonRunner(), "DaemonRunner");
	    // 设置为daemon线程
	    thread.setDaemon(true);
	    thread.start();
	}

	static class DaemonRunner implements Runnable {
		@Override
		public void run() {
			try {
				SleepUtils.second(10);
			} finally {
				System.out.println("DaemonThread finally run.");
			}
		}
	}

}
