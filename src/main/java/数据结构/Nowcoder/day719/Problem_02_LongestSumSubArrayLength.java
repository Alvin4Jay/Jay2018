package 数据结构.Nowcoder.day719;

import java.util.HashMap;

/**
 * 给定一个数组，值可以为正、负和0，请返回累加和为给定值k的最长子数组长度。
 */
public class Problem_02_LongestSumSubArrayLength {
	//O(N) O(N)
	public static int maxLength(int[] arr, int k) {
		if (arr == null || arr.length == 0) {
			return 0;
		}
		//key为累加和第一次出现的和，value为index索引位置
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		// important 保持从0位置开始的累加和为k的子数组的计算逻辑，
		// 表示什么都不加sum=0，出现在-1位置
		map.put(0, -1);
		int len = 0; //最长长度
		int sum = 0; //一轮的和(累加和)
		for (int i = 0; i < arr.length; i++) {
			sum += arr[i];
			if (map.containsKey(sum - k)) { //i位置 大于 sum-k出现的位置 之前
				len = Math.max(i - map.get(sum - k), len); //找到，长度更新
			}
			if (!map.containsKey(sum)) {  //sum第一次加入map
				map.put(sum, i); //记录第一次出现的累加和
			}
		}
		return len;
	}

	public static int[] generateArray(int size) {
		int[] result = new int[size];
		for (int i = 0; i != size; i++) {
			result[i] = (int) (Math.random() * 11) - 5;
		}
		return result;
	}

	public static void printArray(int[] arr) {
		for (int i = 0; i != arr.length; i++) {
			System.out.print(arr[i] + " ");
		}
		System.out.println();
	}

	public static void main(String[] args) {
		int[] arr = generateArray(20);
		printArray(arr);
		System.out.println(maxLength(arr, 10));

	}

}
