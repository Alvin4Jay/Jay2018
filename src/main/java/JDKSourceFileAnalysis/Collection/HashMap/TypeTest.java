package JDKSourceFileAnalysis.Collection.HashMap;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Type接口
 *
 * @author xuanjian
 */
public class TypeTest<K, V> {
	public static void main(String[] args) {

		Type[] ts = User.class.getGenericInterfaces();

		for (Type type : ts) {
			Type t;
			Type[] as;
			ParameterizedType p;
			if ((t = type) instanceof ParameterizedType && (p = (ParameterizedType) t).getRawType() == Comparable.class
					&& (as = p.getActualTypeArguments()) != null && as.length == 1 && as[0] == User.class) {
				System.out.println(p.getRawType());
				System.out.println(Arrays.toString(p.getActualTypeArguments()));
				System.out.println("shot");
			}
		}

		new TypeTest<String, Integer>().test();


	}

	private static class User implements Comparable<User> {
		@Override
		public int compareTo(User o) {
			return 0;
		}
	}

	// 泛型数组创建
	@SuppressWarnings("unchecked")
	public void test() {
		Node<K, V>[] nodes = (Node<K, V>[]) new Node[10];
		Node<K, V>[] ns = (Node<K, V>[]) Array.newInstance(Node.class, 10);
		System.out.println(nodes.getClass());
		System.out.println(ns.getClass());
	}

	private class Node<K, V> {
		final K key;
		V value;

		public Node(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}
}
