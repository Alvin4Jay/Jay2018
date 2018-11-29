package JavaMultiThread.ArtofConcurrencyProgramming.Chapter4.$3;

/**
 * Synchronized
 *
 * @author xuanjian.xuwj
 */
public class Synchronized {
	public static void main(String[] args){
		// 同步代码块
	    synchronized (Synchronized.class) {

		}

		// 同步方法
		m();
	}

	public static synchronized void m() {

	}
}
