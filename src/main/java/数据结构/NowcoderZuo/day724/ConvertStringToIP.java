package 数据结构.NowcoderZuo.day724;

// 给定一个全是数字的字符串，返回可以转成合法IP的数量(分为4部分，每部分数字小于256，不能出现 .01.这种情况)
// 例如：101111
// 可以转成：
// 1.0.1.111, 1.0.11.11, 1.0.111.1
// 10.1.1.11, 10.1.11.1, 10.11.1.1
// 101.1.1.1
// 所以返回7
public class ConvertStringToIP {

	public static int convertNum1(String str) {
		if (str == null || str.length() < 4 || str.length() > 12) {//合法ip字符总数, 4<=length<=12
			return 0;
		}
		char[] chas = str.toCharArray();
		return process(chas, 0, 0); //0位置之前已有0个分段，最终能获得的ip种类数
	}

	/**
	 * i位置之前已有p个分段，最终能获得的ip种类数(暴力递归)
	 * @param chas 字符数组
	 * @param i i 当前位置
	 * @param parts 已有p个分段
	 * @return 最终能获得的ip种类数
	 */
	public static int process(char[] chas, int i, int parts) { //0<=i<=n， 0<=parts<=4
		if (i > chas.length || parts > 4) { //捣乱的
			return 0;
		}
		if (i == chas.length) { //一种可能的情况	
			return parts == 4 ? 1 : 0;
		}

		//parts=4, i<n的无效情况包含在下面

		//i+1位置之前已有p+1个分段，最终能获得的ip种类数
		int res = process(chas, i + 1, parts + 1);  //i位置这个字符作为一段ip段，最后能得到的ip种类
		if (chas[i] == '0') { //当前字符就是'0',直接返回
			return res;
		}
		res += process(chas, i + 2, parts + 1); // i i+1两个字符作为一个ip段，最后能得到的ip种类，有可能为0
		if (i + 2 < chas.length) { //尝试三个字符为一个ip段
			int sum = (chas[i] - '0') * 100 + (chas[i + 1] - '0') * 10 + (chas[i + 2] - '0'); //i到i+2三个字符的十进制数
			if (sum < 256) { //一个ip段有效值为0到255，有效
				return res + process(chas, i + 3, parts + 1);// i i+1 i+2作为一个ip段，，最后能得到的ip种类
			} else { //无效
				return res;
			}
		} else {
			return res;
		}
	}

	//动态规划优化
	public static int convertNum2(String str) {
		if (str == null || str.length() < 4 || str.length() > 12) {
			return 0;
		}
		char[] chas = str.toCharArray();
		int size = chas.length;
		int[][] dp = new int[size + 3][5]; //0 1 2 3 4
		dp[size][4] = 1;

		for (int parts = 3; parts >= 0; parts--) {
			//重要 int i = Math.min(size - 1, 3*parts),有可能字符串很短
			for (int i = Math.min(size - 1, 3*parts); i >= parts; i--) {
				dp[i][parts] = dp[i + 1][parts + 1];
				if (chas[i] != '0') {
					dp[i][parts] += dp[i + 2][parts + 1];
					if (i + 2 < chas.length) { //重要
						int sum = (chas[i] - '0') * 100 + (chas[i + 1] - '0') * 10 + (chas[i + 2] - '0');
						if (sum < 256) {
							dp[i][parts] += dp[i + 3][parts + 1];
						}
					}
				}
			}
		}

		return dp[0][0];
	}

	public static String getRandomNumberString() {
		char[] chas = new char[(int) (Math.random() * 10) + 3];
		for (int i = 0; i < chas.length; i++) {
			chas[i] = (char) (48 + (int) (Math.random() * 10)); //'0'---'9'
		}
		return String.valueOf(chas);
	}

	public static void main(String[] args) {
		int testTime = 3000000;
		boolean hasErr = false;
		for (int i = 0; i < testTime; i++) {
			String test = getRandomNumberString();
			if (convertNum1(test) != convertNum2(test)) {
				hasErr = true;
			}
		}
		if (hasErr) {
			System.out.println("233333");
		} else {
			System.out.println("666666");
		}

	}

}
