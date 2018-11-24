package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$2;

import JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1.SleepUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Thread suspend()/resume()/stop()测试
 *
 * @author xuanjian.xuwj
 */
public class SuspendTest {
	public static void main(String[] args){

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

		Thread printThread = new Thread(new Runner(), "PrintThread");
		printThread.setDaemon(true);
		printThread.start();
		SleepUtils.second(3);

		// 暂停线程
		printThread.suspend();
		System.out.println("main suspend PrintThread at " + format.format(new Date()));
		SleepUtils.second(3);

		// 恢复线程
		printThread.resume();
		System.out.println("main resume PrintThread at " + format.format(new Date()));
		SleepUtils.second(3);

		// 停止线程
		printThread.stop();
		System.out.println("main stop PrintThread at " + format.format(new Date()));
		SleepUtils.second(3);
	}

	// 打印线程
	static class Runner implements Runnable {
		@Override
		public void run() {
			SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
			while(true) {
				System.out.println(Thread.currentThread().getName() + " Run at " + format.format(new Date()));
				SleepUtils.second(1);
			}
		}
	}
}
