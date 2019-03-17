package JavaMultiThread.ArtofConcurrencyProgramming.Chapter10;

import java.util.ArrayList;

/**
 * class description here.
 *
 * @author xuanjian
 * @date 2019/03/13
 */
public class Test {
	public static void main(String[] args) {
		/*Class<Object[]> a = Object[].class;
	 	Class<Integer[]> b = Integer[].class;
	 	Class<Long[]> c = Long[].class;
	 	System.out.println( (Object) a == b);*/

		ArrayList<String> list = new ArrayList<>();
		list.add("aaaa");
		list.add("bbbb");
		list.add("ccc");

		String[] array = list.toArray(new String[0]);

		System.out.println(list.get(0) == array[0]);

	}
}
