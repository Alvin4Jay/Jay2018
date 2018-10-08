package 数据结构.Nowcoder.day0830;

import java.util.Scanner;

/**
 * 喜欢的队列
 */
public class Problem_08 {

	public static long number1(int n, int k) {
		return process(1, n, k);
	}

	/**
	 *
	 * @param pre 之前的数是pre
	 * @param n 还有n个格子需要去填
	 * @param k 填的数在1到k之间
	 * @return 从这个位置开始，一共有几种方法
	 */
	public static long process(int pre, int n, int k) {
		if (n == 0) {
			return 1L;
		}
		long sum = 0;
		for (int cur = pre; cur <= k; cur++) {
			sum += process(cur, n - 1, k);
		}
		for (int cur = 1; cur < pre; cur++) {
			sum += (pre % cur) != 0 ? process(cur, n - 1, k) : 0;
		}
		return sum % 1000000007L;
	}
	//number1()动态规划版
	public static long number2(int n, int k) {
		long[][] dp = new long[k + 1][n]; //k+1行，n列
		for (int i = 0; i < k + 1; i++) {
			dp[i][0] = 1L;
		}
		for (int col = 1; col < n; col++) {
			for (int row = 1; row < k + 1; row++) {
				long sum = 0L;
				for (int cur = row; cur <= k; cur++) {
					sum += dp[cur][col - 1];
				}
				for (int cur = 1; cur < row; cur++) {
					sum += (row % cur) != 0 ? dp[cur][col - 1] : 0;
				}
				dp[row][col] = sum % 1000000007L;
			}
		}
		long res = 0L;
		for (int i = 1; i <= k; i++) {
			res += dp[i][n - 1];
			res %= 1000000007L;
		}
		return res;
	}
	//动态规划改进2 sum减去某些值
	public static long number3(int n, int k) {
		long[][] dp = new long[k + 1][n];
		for (int i = 0; i < k + 1; i++) {
			dp[i][0] = 1L;
		}
		for (int col = 1; col < n; col++) {
			long sum = 0;
			for (int row = 1; row < k + 1; row++) {
				sum += dp[row][col - 1]; //先计算前一列的种类和
				sum %= 1000000007L;
			}
			for (int row = 1; row < k + 1; row++) {
				long noInclude = 0L;
				for (int cur = row * 2; cur <= k; cur += row) {
					noInclude += dp[cur][col - 1];
					noInclude %= 1000000007L;
				}
				dp[row][col] = (sum - noInclude) % 1000000007L;
			}
		}
		long res = 0L;
		for (int i = 1; i <= k; i++) {
			res += dp[i][n - 1];
			res %= 1000000007L;
		}
		return res;
	}

	public static long number4(int n, int k) {
		long[] dp = new long[k + 1];
		for (int i = 0; i < k + 1; i++) {
			dp[i] = 1L;
		}
		for (int col = 1; col < n; col++) {
			long sum = 0;
			for (int row = 1; row < k + 1; row++) {
				sum += dp[row];
				sum %= 1000000007L;
			}
			for (int row = 1; row < k + 1; row++) {
				long noInclude = 0L;
				for (int cur = row * 2; cur <= k; cur += row) {
					noInclude += dp[cur];
					noInclude %= 1000000007L;
				}
				dp[row] = (sum - noInclude) % 1000000007L;
			}
		}
		long res = 0L;
		for (int i = 1; i <= k; i++) {
			res += dp[i];
			res %= 1000000007L;
		}
		return res;
	}

	public static void main(String[] args) {
		System.out.println(number1(6, 9));
		System.out.println(number2(6, 9));
		System.out.println(number3(6, 9));
		System.out.println(number4(6, 9));

		Scanner in = new Scanner(System.in);

		while (in.hasNextInt()) {
			int n = in.nextInt();
			int k = in.nextInt();
			System.out.println(number2(n, k));
		}
		in.close();
	}

}
