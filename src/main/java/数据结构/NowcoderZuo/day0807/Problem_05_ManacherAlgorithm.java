package 数据结构.NowcoderZuo.day0807;

/**
 * 给定一个字符串str，返回str中最长回文子串的长度O(N)
 */
public class Problem_05_ManacherAlgorithm {
	//字符串预处理
	public static char[] manacherString(String str) {
		char[] charArr = str.toCharArray(); //n
		char[] res = new char[str.length() * 2 + 1]; //2n+1
		int index = 0;
		for (int i = 0; i != res.length; i++) {
			res[i] = (i & 1) == 0 ? '#' : charArr[index++];
		}
		return res;
	}
	//Manacher算法
	public static int maxLcpsLength(String str) {
		if (str == null || str.length() == 0) {
			return 0;
		}
		char[] charArr = manacherString(str); //处理后的字符数组
		int[] pArr = new int[charArr.length]; //处理厚后的字符串回文半径数组
		int index = -1; //回文右边界对应的回文中心
		int pR = -1; //回文右边界
		int max = Integer.MIN_VALUE; //处理后的字符串最大回文半径
		for (int i = 0; i != charArr.length; i++) {
			pArr[i] = pR > i ? Math.min(pArr[2 * index - i], pR - i) : 1; //在右边界里或者不在
			while (i + pArr[i] < charArr.length && i - pArr[i] > -1) { //暴力扩
				if (charArr[i + pArr[i]] == charArr[i - pArr[i]])
					pArr[i]++;
				else {
					break;
				}
			}
			if (i + pArr[i] > pR) { //每次检查是否更新pR，index
				pR = i + pArr[i];
				index = i;
			}
			max = Math.max(max, pArr[i]); //每次检查是否更新max(处理后的字符串最长回文半径)
		}
		return max - 1; //原字符串的最长子串长度为max-1
	}

	//进阶：末尾添加最短字符串(最短)，使之成为回文串
	public static String shortestEnd(String str) {
		if (str == null || str.length() == 0) {
			return null;
		}
		char[] charArr = manacherString(str);
		int[] pArr = new int[charArr.length];
		int index = -1;
		int pR = -1;
		int maxContainsEnd = -1; //记录处理后字符串pR第一次到达右边时的回文子串半径
		for (int i = 0; i != charArr.length; i++) {
			pArr[i] = pR > i ? Math.min(pArr[2 * index - i], pR - i) : 1;
			while (i + pArr[i] < charArr.length && i - pArr[i] > -1) {
				if (charArr[i + pArr[i]] == charArr[i - pArr[i]])
					pArr[i]++;
				else {
					break;
				}
			}
			if (i + pArr[i] > pR) {
				pR = i + pArr[i];
				index = i;
			}
			if (pR == charArr.length) { //每次检查pR == charArr.length，退出循环
				maxContainsEnd = pArr[i];
				break;
			}
		}
		//maxContainsEnd - 1表示原字符串中pR第一次到达右边时构成回文的子字符串长度
		//原字符串除去包含最后一个字符构成的最长回文的子字符串的字符串数组
		char[] res = new char[str.length() - maxContainsEnd + 1];
		for (int i = 0; i < res.length; i++) {
			res[res.length - 1 - i] = charArr[i * 2 + 1]; //逆序，处理后的字符串奇数位置
		}
		return String.valueOf(res);
	}

	public static void main(String[] args) {
		String str1 = "abc1234321ab";
		System.out.println(maxLcpsLength(str1));

		String str2 = "abcd123321";
		System.out.println(shortestEnd(str2));

	}

}
