package 数据结构.NowcoderZuo.day0807;

import java.util.Arrays;

/**
 * 在两个长度相等的排序数组上找到上中位数O(lgN)
 */
public class Problem_01_FindUpMedian {
	//二分
	public static int getUpMedian(int[] arr1, int[] arr2) {
		if (arr1 == null || arr2 == null || arr1.length != arr2.length) { //长度不相等，返回
			throw new RuntimeException("Your arr is invalid!");
		}
		int start1 = 0; //第一个数组标号
		int end1 = arr1.length - 1;
		int start2 = 0;//第一个数组标号
		int end2 = arr2.length - 1;
		int mid1 = 0; //第一个数组的上中位数
		int mid2 = 0; //第二个数组的上中位数
		int offset = 0; //根据数组奇偶决定的0/1
		while (start1 < end1) { //不是只有一个元素
			mid1 = (start1 + end1) / 2;
			mid2 = (start2 + end2) / 2;
			// 元素个数为奇数，offset为0，元素个数为偶数，offset为1。
			offset = ((end1 - start1 + 1) & 1) ^ 1;
			if (arr1[mid1] > arr2[mid2]) {
				end1 = mid1;
				start2 = mid2 + offset;
			} else if (arr1[mid1] < arr2[mid2]) {
				start1 = mid1 + offset;
				end2 = mid2;
			} else {
				return arr1[mid1]; //相等，返回
			}
		}
		return Math.min(arr1[start1], arr2[start2]); //一个数组只剩1个元素，返回较小的元素
	}

	// For test, this method is inefficient but absolutely right
	public static int findForTest(int[] arr1, int[] arr2) {
		if (arr1 == null || arr2 == null || arr1.length != arr2.length) {
			throw new RuntimeException("Your arr is invalid!");
		}
		int[] arrAll = new int[arr1.length + arr2.length];
		for (int i = 0; i != arr1.length; i++) {
			arrAll[i * 2] = arr1[i];
			arrAll[i * 2 + 1] = arr2[i];
		}
		Arrays.sort(arrAll);
		return arrAll[(arrAll.length / 2) - 1];
	}

	public static int[] generateSortedArray(int len, int maxValue) {
		int[] res = new int[len];
		for (int i = 0; i != len; i++) {
			res[i] = (int) (Math.random() * (maxValue + 1));
		}
		Arrays.sort(res);
		return res;
	}

	public static void printArray(int[] arr) {
		for (int i = 0; i != arr.length; i++) {
			System.out.print(arr[i] + " ");
		}
		System.out.println();
	}

	public static void main(String[] args) {
		int len = 10;
		int maxValue1 = 20;
		int maxValue2 = 50;
		int[] arr1 = generateSortedArray(len, maxValue1);
		int[] arr2 = generateSortedArray(len, maxValue2);
		printArray(arr1);
		printArray(arr2);
		System.out.println(getUpMedian(arr1, arr2));
		System.out.println(findForTest(arr1, arr2));
		System.out.println(getUpMedian(new int[]{0,1,2}, new int[]{3,4,5}));
		System.out.println(findForTest(new int[]{0,1,2}, new int[]{3,4,5}));
	}

}
