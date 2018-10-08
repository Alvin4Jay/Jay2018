package 数据结构.Nowcoder.day719;

/**
 * 给定一个数组，值全是正数，请返回累加和为给定值k的最长子数组长度。
 */
public class Problem_02_LongestSumSubArrayLengthInPositiveArray {
	//O(N) O(1)
	public static int getMaxLength(int[] arr, int k) {
		if (arr == null || arr.length == 0 || k <= 0) { //k<=0直接返回0
			return 0;
		}
		int left = 0; //左指针(包含)
		int right = 0; //右指针(包含)
		int sum = arr[0]; //left到right的和
		int len = 0; //和为k的最长子数组最大长度
		while (right < arr.length) {
			if (sum == k) {
				len = Math.max(len, right - left + 1);
				sum -= arr[left++];//从left+1开始下一轮，以left+1开始的最长子数组长度计算
			} else if (sum < k) {
				right++;
				if (right == arr.length) { //从这里开始，后面从left开始的所有子数组元素之和都不可能为k
					break;
				}
				sum += arr[right];
			} else {
				sum -= arr[left++]; //从left+1开始下一轮，以left+1开始的最长子数组长度计算
			}
		}
		return len;
	}

	public static int[] generatePositiveArray(int size) {
		int[] result = new int[size];
		for (int i = 0; i != size; i++) {
			result[i] = (int) (Math.random() * 10) + 1;
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
		int len = 20;
		int k = 15;
		int[] arr = generatePositiveArray(len);
		printArray(arr);
		System.out.println(getMaxLength(arr, k));

	}

}
