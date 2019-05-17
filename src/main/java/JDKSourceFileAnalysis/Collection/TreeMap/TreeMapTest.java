package JDKSourceFileAnalysis.Collection.TreeMap;

import java.util.TreeMap;

/**
 * TreeMap Test
 *
 * @author xuanjian
 */
public class TreeMapTest {
	public static void main(String[] args) {

		TreeMap<String, Integer> map = new TreeMap<>();

		map.put("c", 3);
		map.put("d", 2);
		map.put("a", 5);
		map.put("e", 1);
		map.put("b", 4);

		map.forEach((key, value) -> System.out.println(key + "--" + value));

	}
}
