package 数据结构.Nowcoder.day719;

/**
 有一排正数，玩家A和玩家B都可以看到。
 每位玩家在拿走数字的时候，都只能从最左和最右的数中选择一个。
 玩家A先拿，玩家B再拿，两人交替拿走所有的数字，
 两人都力争自己拿到的数的总和比对方多。请返回最后获胜者的分数。

 例如：
 5,2,3,4
 玩家A先拿，当前他只能拿走5或者4。
 如果玩家A拿走5，那么剩下2，3，4。轮到玩家B，此时玩家B可以选择2或4中的一个，…
 如果玩家A拿走4，那么剩下5，2，3。轮到玩家B，此时玩家B可以选择5或3中的一个，…
 */
public class Problem_03_CardsInLine {

	//决策(暴力递归)
	public static int win1(int[] arr) {
		if (arr == null || arr.length == 0) {
			return 0;
		}
		//返回先发者或者后发者中分数最大的那个
		return Math.max(f(arr, 0, arr.length - 1), s(arr, 0, arr.length - 1));
	}
	//作为先发者，最终获得的最优分数(收益)
	/**
	 * 求先发者最终的分数
	 * @param arr 卡片
	 * @param i 左边界
	 * @param j 右边界
	 * @return 先发者最终的分数
	 */
	public static int f(int[] arr, int i, int j) {
		if (i == j) {  //只有一个数
			return arr[i];
		}
		return Math.max(arr[i] + s(arr, i + 1, j), arr[j] + s(arr, i, j - 1));
	}
	//作为后发者，最终获得的最优分数(收益)
	/**
	 * 求后发者最终的最优分数
	 * @param arr 卡片
	 * @param i 左边界
	 * @param j 右边界
	 * @return 后发者最终的最优分数
	 */
	public static int s(int[] arr, int i, int j) {
		if (i == j) {  //只有一个数，返回0
			return 0;
		}
		return Math.min(f(arr, i + 1, j), f(arr, i, j - 1)); //后发者只能拿到相对小的那个分数
	}

	//win1动态规划 O(n²)
	public static int win2(int[] arr) {
		if (arr == null || arr.length == 0) {
			return 0;
		}
		int[][] f = new int[arr.length][arr.length]; //0--N-1
		int[][] s = new int[arr.length][arr.length]; //0--N-1
		for (int j = 0; j < arr.length; j++) { //列
			f[j][j] = arr[j];
			s[j][j] = 0;
			for (int i = j - 1; i >= 0; i--) { //第几行，计算出j列的所有dp元素
				f[i][j] = Math.max(arr[i] + s[i + 1][j], arr[j] + s[i][j - 1]);
				s[i][j] = Math.min(f[i + 1][j], f[i][j - 1]);
			}
		}
		return Math.max(f[0][arr.length - 1], s[0][arr.length - 1]);
	}

	//只考虑先发者的情况
	public static int win3(int[] arr) {
		if (arr == null || arr.length == 0) {
			return 0;
		}
		int sum = 0;
		for (int i = 0; i < arr.length; i++) { //求的总分数
			sum += arr[i];
		}
		int scores = p(arr, 0, arr.length - 1); //作为先发者获得的最好分数
		return Math.max(sum - scores, scores);
	}
	//只关心先发者的最终分数
	public static int p(int[] arr, int i, int j) {
		if (i == j) {  //i == j 只有一个数
			return arr[i];
		}
		if (i + 1 == j) { //只有两个数
			return Math.max(arr[i], arr[j]);
		}
		return Math.max(
				//i+1 ... j
				arr[i] + Math.min(p(arr, i + 2, j), p(arr, i + 1, j - 1)), //不归我决定
				//i ... j-1
				arr[j] + Math.min(p(arr, i + 1, j - 1), p(arr, i, j - 2)));
	}

	//win3动态规划 O(n²)
	public static int win4(int[] arr) {
		if (arr == null || arr.length == 0) {
			return 0;
		}
		if (arr.length == 1) {
			return arr[0];
		}
		if (arr.length == 2) {
			return Math.max(arr[0], arr[1]);
		}
		//初始化
		int sum = 0;
		for (int i = 0; i < arr.length; i++) { //求和
			sum += arr[i];
		}
		int[][] dp = new int[arr.length][arr.length]; //NxN
		for (int i = 0; i < arr.length - 1; i++) {
			dp[i][i] = arr[i];
			dp[i][i + 1] = Math.max(arr[i], arr[i + 1]);
		}
		dp[arr.length - 1][arr.length - 1] = arr[arr.length - 1];

		//dp计算
		for (int k = 2; k < arr.length; k++) { //计算斜边
			for (int j = k; j < arr.length; j++) { //列
				int i = j - k; //行，斜向下dp计算
				dp[i][j] = Math.max(arr[i] + Math.min(dp[i + 2][j], dp[i + 1][j - 1]),
						arr[j] + Math.min(dp[i + 1][j - 1], dp[i][j - 2]));
			}
		}
		return Math.max(dp[0][arr.length - 1], sum - dp[0][arr.length - 1]);
	}

	public static int[] generateRondomArray() {
		int[] res = new int[(int) (Math.random() * 20) + 1];
		for (int i = 0; i < res.length; i++) {
			res[i] = (int) (Math.random() * 20) + 1;
		}
		return res;
	}

	public static void main(String[] args) {
		int testTime = 50000;
		boolean err = false;
		for (int i = 0; i < testTime; i++) {
			int[] arr = generateRondomArray();
			int r1 = win1(arr);
			int r2 = win2(arr);
			int r3 = win3(arr);
			int r4 = win4(arr);
			if (r1 != r2 || r1 != r3 || r1 != r4) {
				err = true;
			}
		}
		if (err) {
			System.out.println("2333333333");
		} else {
			System.out.println("6666666666");
		}
	}

}
