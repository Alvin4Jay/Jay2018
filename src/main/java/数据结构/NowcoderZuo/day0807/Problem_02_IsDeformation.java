package 数据结构.NowcoderZuo.day0807;

import java.util.HashMap;
import java.util.Map;

/**
 * 判断两个字符串是否互为变形词
 */
public class Problem_02_IsDeformation {

	public static boolean isDeformation(String str1, String str2) {
		if (str1 == null || str2 == null || str1.length() != str2.length()) {
			return false;
		}
		char[] chas1 = str1.toCharArray();
		char[] chas2 = str2.toCharArray();
		int[] map = new int[256];
		for (int i = 0; i < chas1.length; i++) {
			map[chas1[i]]++;
		}
		for (int i = 0; i < chas2.length; i++) {
			if (map[chas2[i]] == 0) { //减1之前已经等于0，表明某个字符次数不相等
				return false;
			}
			map[chas2[i]] --;
		}
		return true;
	}

	public static boolean isDeformation2(String str1, String str2) {
		if (str1 == null || str2 == null || str1.length() != str2.length()) {
			return false;
		}
		char[] chas1 = str1.toCharArray();
		char[] chas2 = str2.toCharArray();
		Map<Character, Integer> map = new HashMap<Character, Integer>();
 		for (int i = 0; i < chas1.length; i++) {
 			if(map.containsKey(chas1[i])){
 				map.put(chas1[i], map.get(chas1[i])+1);
			}else {
 				map.put(chas1[i], 1);
			}
		}
		for (int i = 0; i < chas2.length; i++) {
			if(map.containsKey(chas1[i])){
				if(map.get(chas2[i]) == 0){
					return false;
				}else {
					map.put(chas2[i], map.get(chas2[i])-1);
				}
			}else {
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		String A = "abcabcabc";
		String B = "bcacbaacb";
		String C = "abcc";
		String D = "abbc";
		System.out.println(isDeformation(A, B));
		System.out.println(isDeformation2(A, B));
		System.out.println(isDeformation2(C, D));

	}

}
