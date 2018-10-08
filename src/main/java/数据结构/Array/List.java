package 数据结构.Array;

/**
 * List可以随机访问,不需要遍历
 */
public interface List {
	void add(Object o);

	void add(int index, Object o);

	Object get(int index);

	Object remove(int index);

	int size();



}
