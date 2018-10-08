package 数据结构.Nowcoder.day0823;

import java.util.Scanner;

public class Problem_03 {
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String str = sc.nextLine();

		/*int max = 1;  //全局最大
		int count = 1; //一轮最大
		for (int i = 1; i < str.length(); i++) {
			//count++;
			if (str.charAt(i) == str.charAt(i - 1)) {
				count = 1;
			}else {
				count ++;
			}
			max = Math.max(max, count);
		}
		System.out.println(max);*/

		int res = 1;
		int[] dp = new int[str.length()];
		dp[0] = 1;
		for (int i = 1; i < dp.length; i++) {
			if(str.charAt(i) == str.charAt(i-1)){
				dp[i] = 1;
			}else {
				dp[i] = dp[i-1] + 1;
			}
			res = Math.max(res, dp[i]);
		}
		System.out.println(res);
		sc.close();
	}
}