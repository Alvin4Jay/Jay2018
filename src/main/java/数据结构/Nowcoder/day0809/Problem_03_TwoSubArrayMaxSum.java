package 数据结构.Nowcoder.day0809;

public class Problem_03_TwoSubArrayMaxSum {
	//预处理数组O(n) O(n)
	public static int twoSubArrayMaxSum(int[] arr) {
		if (arr == null || arr.length < 2) {
			return 0;
		}
		int[] rArray = new int[arr.length]; //右边预处理数组
		int max = Integer.MIN_VALUE;
		int cur = 0;
		for (int i = arr.length - 1; i > 0; i--) {
			cur += arr[i];
			max = Math.max(max, cur);
			rArray[i] = max;
			cur = cur < 0 ? 0 : cur;
		}
		int res = Integer.MIN_VALUE;  //不相交的两个子数组最大和
		max = Integer.MIN_VALUE; //从左边开始计算
		cur = 0;
		for (int i = 0; i < arr.length - 1; i++) {
			cur += arr[i];
			max = Math.max(max, cur);
			res = Math.max(res, max + rArray[i + 1]);
			cur = cur < 0 ? 0 : cur;
		}
		return res;
	}

	// for test
	public static int rightAnswer(int[] arr) {
		if (arr == null || arr.length < 2) {
			return 0;
		}
		int res = Integer.MIN_VALUE;
		for (int p = 0; p < arr.length - 1; p++) {
			res = Math.max(res, maxSum(arr, 0, p) + maxSum(arr, p + 1, arr.length - 1));
		}
		return res;
	}

	// for test
	public static int maxSum(int[] arr, int l, int r) {
		int max = Integer.MIN_VALUE;
		int cur = 0;
		for (int i = l; i <= r; i++) {
			cur += arr[i];
			max = Math.max(max, cur);
			cur = cur < 0 ? 0 : cur;
		}
		return max;
	}

	// for test
	public static int[] generateRandomArray() {
		int[] res = new int[(int) (Math.random() * 10) + 1];
		for (int i = 0; i < res.length; i++) {
			res[i] = (int) (Math.random() * 20) - 10;
		}
		return res;
	}

	// for test
	public static void main(String[] args) {
		int testTime = 5000000;
		boolean hasErr = false;
		for (int i = 0; i < testTime; i++) {
			int[] test = generateRandomArray();
			if (twoSubArrayMaxSum(test) != rightAnswer(test)) {
				hasErr = true;
			}
		}
		if (hasErr) {
			System.out.println("23333333");
		} else {
			System.out.println("66666666");
		}

	}

}
