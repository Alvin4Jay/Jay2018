package JavaMultiThread.ArtofConcurrencyProgramming.Chapter3;

/**
 * Final Field Test
 * @author xuanjian.xuwj
 */
public class FinalExample {

	int i;
	final int j;

	static FinalExample obj;

	public FinalExample() {
		i = 1;
		j = 2;
	}

	public static void write() {
		obj = new FinalExample();
	}

	public static void read() {
		FinalExample object = obj;
		int a = object.i;
		int b = object.j;
	}

}
