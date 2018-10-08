package 数据结构.Nowcoder.day0830;

import java.util.Scanner;
import java.util.Arrays;

/**
 * 疯狂队列 (贪心) 先排序
 */
public class Problem_07 {

	public static int maxMad(int[] arr) {
		Arrays.sort(arr);
		int res = arr[arr.length - 1] - arr[0];
		int maxI = arr.length - 2;
		int minI = 1;
		while (minI < maxI) {
			res += arr[maxI + 1] - arr[minI];
			res += arr[maxI] - arr[minI - 1];
			maxI--;
			minI++;
		}
		if (minI == maxI) { //奇数情况
			res += Math.max(arr[minI] - arr[minI - 1], arr[minI + 1] - arr[minI]);
		}
		//直接是偶数情况
		return res;
	}

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		while (in.hasNextInt()) {
			int n = in.nextInt();
			int[] arr = new int[n];
			for (int i = 0; i < n; i++) {
				arr[i] = in.nextInt();
			}
			System.out.println(maxMad(arr));
		}
		in.close();
	}
}
