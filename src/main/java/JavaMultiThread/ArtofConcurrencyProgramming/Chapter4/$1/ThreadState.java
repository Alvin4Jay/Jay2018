package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1;

/**
 * 线程的状态
 *
 * @author xuanjian.xuwj
 */
public class ThreadState {
	public static void main(String[] args){
		new Thread(new TimeWaiting(), "TimeWaitingThread").start();
		new Thread(new Waiting(), "WaitingThread").start();
		new Thread(new Blocked(), "BlockedThread-1").start();
		new Thread(new Blocked(), "BlockedThread-2").start();
	}

	// TIME_WAITING sleep
	static class TimeWaiting implements Runnable {
		@Override
		public void run() {
			while(true) {
				SleepUtils.second(100);
			}
		}
	}

	// WAITING on object monitor
	static class Waiting implements Runnable {
		@Override
		public void run() {
			while (true) {
				synchronized (Waiting.class) {
					try {
						// wait() 方法释放锁
						Waiting.class.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	// 获得锁：TIME_WAITING sleep；没获得锁： BLOCKED on object monitor
	static class Blocked implements Runnable {
		@Override
		public void run() {
			synchronized (Blocked.class) {
				while (true) {
					// sleep() 不释放锁
					SleepUtils.second(100);
				}
			}
		}
	}

}
