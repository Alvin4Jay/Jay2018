package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$1;

import java.util.concurrent.TimeUnit;

/**
 * SleepUtils
 *
 * @author xuanjian.xuwj
 */
public class SleepUtils {
	public static void second(long seconds){
		try {
			TimeUnit.SECONDS.sleep(seconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
