package 数据结构.NowcoderZuo.day0821;

import java.util.Arrays;
import java.util.Comparator;

public class Problem_04_LowestLexicography {
/*
	给定字符串数组，返回连接后的字典序最小的串
*/
	public static class MyComparator implements Comparator<String> {
		@Override
		public int compare(String a, String b) {
			return (a + b).compareTo(b + a);
		}
	}
	
	
	public static class Worker{
		public int age;
		public int id;
		public String name;
	}
	
	public static class MyWorker implements Comparator<Worker> {

		@Override
		public int compare(Worker o1, Worker o2) {
			if(o1.age < o2.age) {
				return -1;
			}else if(o1.age == o2.age) {
				return 0;
			}else {
				return 1;
			}
		}

	}
	
	
	

	public static String lowestString(String[] strs) {
		
		
		Worker[] works = new Worker[100];
		// works[1] = ...
		
		Arrays.sort(works,new MyWorker());
		
		
		
		
		
		if (strs == null || strs.length == 0) {
			return "";
		}
		Arrays.sort(strs, new MyComparator());
		String res = "";
		for (int i = 0; i < strs.length; i++) {
			res += strs[i];
		}
		return res;
	}

	public static void main(String[] args) {
		String[] strs1 = { "jibw", "ji", "jp", "bw", "jibw" };
		System.out.println(lowestString(strs1));

		String[] strs2 = { "ba", "b" };
		System.out.println(lowestString(strs2));

	}

}
