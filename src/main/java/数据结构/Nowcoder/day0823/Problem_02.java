package 数据结构.Nowcoder.day0823;

import java.util.Arrays;
import java.util.Scanner;

public class Problem_02 {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int n = sc.nextInt();
		int seq[] = new int[n];
		for (int i = 0; i < n; i++) {
			seq[i] = sc.nextInt();
		}
		if (isArithmeticSequence(seq, n))
			System.out.println("Possible");
		else
			System.out.println("Impossible");
		sc.close();

	}

	public static boolean isArithmeticSequence(int[] seq, int n) {
		int sum = 0;
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < n; i++) {
			sum += seq[i];
			min = Math.min(min, seq[i]);
		}
		if ((2 * (sum - n * min)) % (n * (n - 1)) == 0) //等差数列求和公式 O(n)
			return true;
		else
			return false;
	}
	//O(nlgn) 排序+遍历
	public static boolean isArithmeticSequence2(int[] seq, int n) {
		Arrays.sort(seq);

		int d = seq[1] - seq[0];

		for (int i = 1; i < seq.length-1; i++) {
			if(seq[i+1] != (seq[i]+d) ){
				return false;
			}
		}
		return true;
	}
}
