package 数据结构.Nowcoder.day719;

/**
 1、已知一个字符串都是由左括号(和右括号)组成，判断该字符串是否是有效的括号组合。

 例子：
 有效的括号组合:()(),(()),(()())  //以左括号开头
 无效的括号组合:(,()),((),()(()


 2、题目进阶：
 已知一个字符串都是由左括号(和右括号)组成，返回最长有效括号子串的长度。

 */
public class Problem_01_ParenthesesProblem {
	/**O(N)
	 * 一个变量state，遇到左括号++，遇到右括号--，遍历中途state小于0，返回false;否则遍历结束，
	 * 如果state为0，返回true，不然返回false
	 * @param str
	 * @return
	 */
	public static boolean isValid(String str) {
		if (str == null || str.equals("")) {
			return false;
		}
		char[] chas = str.toCharArray();
		int status = 0;
		for (int i = 0; i < chas.length; i++) {
			if (chas[i] != ')' && chas[i] != '(') { //这一步可以不加
				return false;
			}
			if (chas[i] == ')' && --status < 0) { //右括号多了，无法匹配左括号
				return false;
			}
			if (chas[i] == '(') { //遇到左括号
				status++;
			}
		}
		return status == 0;
	}

	//动态规划，dp[i]以某个字符结尾i的情况下，得到最长有效括号子串的长度O(N),已知dp[0...i] 求dp[i+1]
	public static int maxLength(String str) {
		if (str == null || str.equals("")) {
			return 0;
		}
		char[] chas = str.toCharArray();
		int[] dp = new int[chas.length]; //状态数组，dp[0]=0;
		int pre = 0;  //表示"("的位置
		int res = 0;  //最长长度
		for (int i = 1; i < chas.length; i++) {
			if (chas[i] == ')') {
				pre = i - dp[i - 1] - 1; //表示"("的位置
				if (pre >= 0 && chas[pre] == '(') { //有效
					dp[i] = dp[i - 1] + 2 + (pre > 0 ? dp[pre - 1] : 0); //跳一步
				} //否则dp[i] = 0
			}
			/*else {
				dp[i] = 0; //"("
			}*/
			res = Math.max(res, dp[i]); //最后每次计算，都更新res
		}
		return res;
	}

	public static void main(String[] args) {
		String str1 = "((())())";
		System.out.println(isValid(str1));
		System.out.println(maxLength(str1));

		String str2 = "(())(()(()))";
		System.out.println(isValid(str2));
		System.out.println(maxLength(str2));

		String str3 = "()(()()(";
		System.out.println(isValid(str3));
		System.out.println(maxLength(str3));

		String str4 = "()(()())";
		System.out.println(isValid(str4));
		System.out.println(maxLength(str4));

	}
}
