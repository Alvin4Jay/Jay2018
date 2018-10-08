package 数据结构.Nowcoder.day719;

import java.util.HashMap;

/**
 * 给定一个数组，值可以为正、负和0，请返回累加和小于等于k的最长子数组长度。
 */
public class Problem_02_LongestSubarrayLessSumAwesomeSolution {
	//O(N) O(N)
	// So great, first time, my book need modify, add this solution into book
	public static int maxLengthAwesome(int[] arr, int k) {
		if (arr == null || arr.length == 0) {
			return 0;
		}
		int[] sums = new int[arr.length]; //最小和
		//key 元素index, value最小和右边界
		HashMap<Integer, Integer> ends = new HashMap<Integer, Integer>(); //最小和右边界
		sums[arr.length - 1] = arr[arr.length - 1]; //从末尾开始
		ends.put(arr.length - 1, arr.length - 1);
		for (int i = arr.length - 2; i >= 0; i--) { //更新策略
			if (sums[i + 1] < 0) {
				sums[i] = arr[i] + sums[i + 1];
				ends.put(i, ends.get(i + 1));
			} else {
				sums[i] = arr[i];
				ends.put(i, i);
			}
		}

		int end = 0; //下一坨起点
		int sum = 0; //一轮累加和
		int res = 0; //符合条件的最长子数组长度
		for (int i = 0; i < arr.length; i++) { //从某个位置开始的累加和<=k的最长子数组计算
			//这里直接判断减去头元素之后再加1坨后的sum是否小于k,
			// 这里先判断减去头元素之后的sum<k无意义(长度)
			while (end < arr.length && sum + sums[end] <= k) {
				sum += sums[end];
				end = ends.get(end) + 1; //下一坨的起点
			}
			res = Math.max(res, end - i); //更新res
			//从0开始每次可能出现检查不符合，出现 end == i，sum不用变换
			/*if(end > i){
				sum -= arr[i];
			}else if(end == i){
				sum -= 0;
			}*/
			sum -= (end > i ? arr[i] : 0);
			//从0开始每次可能出现检查不符合，出现  end == i，end变为i+1
			/*if(end == i){
				end = i+1; //下一坨起点
			}*/
			end = Math.max(end, i + 1);
		}
		return res;
	}

	//O(NlgN) O(N)
	public static int maxLength(int[] arr, int k) {
		int[] h = new int[arr.length + 1]; //help数组
		int sum = 0;
		h[0] = sum;
		for (int i = 0; i != arr.length; i++) {
			sum += arr[i];
			h[i + 1] = Math.max(sum, h[i]); //计算help数组，递增
		}

		sum = 0;
		int res = 0; //符合条件的子数组最长长度
		int pre = 0; //help数组中搜索到的>=k位置的搜索结果
		int len = 0; //本轮长度
		for (int i = 0; i != arr.length; i++) {
			sum += arr[i];
			pre = getLessIndex(h, sum - k); //>=k的位置， O(lgN)
			len = pre == -1 ? 0 : i - pre + 1;
			res = Math.max(res, len);
		}
		return res;
	}

	public static int getLessIndex(int[] arr, int num) {
		int low = 0;
		int high = arr.length - 1;
		int mid = 0;
		int res = -1;
		while (low <= high) {
			mid = (low + high) / 2;
			if (arr[mid] >= num) {
				res = mid;
				high = mid - 1;
			} else {
				low = mid + 1;
			}
		}
		return res;
	}

	// for test
	public static int[] generateRandomArray(int len, int maxValue) {
		int[] res = new int[len];
		for (int i = 0; i != res.length; i++) {
			res[i] = (int) (Math.random() * maxValue) - (maxValue / 3);
		}
		return res;
	}

	public static void main(String[] args) {
		for (int i = 0; i < 1000000; i++) {
			int[] arr = generateRandomArray(10, 20);
			int k = (int) (Math.random() * 20) - 5;
			if (maxLengthAwesome(arr, k) != maxLength(arr, k)) {
				System.out.println("oops!");
			}
		}

	}

}
