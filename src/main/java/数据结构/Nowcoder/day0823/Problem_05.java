package 数据结构.Nowcoder.day0823;

/**
 * 走的走法
 */
public class Problem_05 {
	/**
	 * 走法
	 * @param N N个位置
	 * @param P 开始位置
	 * @param K 第k分钟
	 * @param T 最终位置
	 * @return 走法
	 */
	public static int f1(int N, int P, int K, int T) {
		if (N < 2 || P < 0 || K < 1 || T < 0 || P >= N || T >= N) { //无效状态
			return 0;
		}
		if (K == 1) { //从第一分钟开始算，第一分钟的时候
			return T == P ? 1 : 0;
		}
		if (T == 0) {
			return f1(N, P, K - 1, 1);
		}
		if (T == N - 1) {
			return f1(N, P, K - 1, T - 1);
		}
		return f1(N, P, K - 1, T - 1) + f1(N, P, K - 1, T + 1);
	}
	//动态规划
	public static int f2(int N, int P, int K, int T) {
		if (N < 2 || P < 0 || K < 1 || T < 0 || P >= N || T >= N) {
			return 0;
		}
		int[][] dp = new int[K][N];
		dp[0][P] = 1; //k==1
		for (int i = 1; i < K; i++) { //从左到右，从上到下
			dp[i][0] = dp[i - 1][1];
			dp[i][N - 1] = dp[i - 1][N - 2];
			for (int j = 1; j < N - 1; j++) {
				dp[i][j] = dp[i - 1][j - 1] + dp[i - 1][j + 1];
			}
		}
		return dp[K - 1][T]; // K T
	}
	//空间压缩
	public static int f3(int N, int P, int K, int T) {
		if (N < 2 || P < 0 || K < 1 || T < 0 || P >= N || T >= N) {
			return 0;
		}
		int[] dp = new int[N];
		dp[P] = 1;
		int pre = 0;
		int tmp = 0;
		for (int i = 1; i < K; i++) {
			pre = dp[0];
			dp[0] = dp[1];
			for (int j = 1; j < N - 1; j++) {
				tmp = dp[j];
				dp[j] = pre + dp[j + 1];
				pre = tmp;
			}
			dp[N - 1] = pre;
		}
		return dp[T];
	}

	public static void main(String[] args) {
		for (int i = 0; i < 3000000; i++) {
			int N = (int) (Math.random() * 5) + 5;
			int P = (int) (Math.random() * N);
			int K = (int) (Math.random() * 10) + 2;
			int T = (int) (Math.random() * N);
			int res1 = f1(N, P, K, T);
			int res2 = f2(N, P, K, T);
			int res3 = f3(N, P, K, T);
			if (res1 != res2 || res1 != res3) {
				System.out.println("Fuck man!");
				break;
			}
		}

	}

}
