package 数据结构.Nowcoder.dp;

import java.util.Scanner;

/**
 * Longest Ordered Subsequence 最长递增子序列
 */
public class Poj2533 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextInt()){
            int N = sc.nextInt();
            int[] arr = new int[N];
            for (int i = 0; i < N; i++) {
                arr[i] = sc.nextInt();
            }
            System.out.println(maxLength(arr));
        }
    }
    //dp[i]以每个位置结尾的情况下能达到的最长子序列长度
    public static int maxLength(int[] arr){
        if(arr == null || arr.length == 0) return 0;

        int maxLength = 1; //最大长度
        int[] dp = new int[arr.length];
        for (int i = 0; i < dp.length; i++) {
            dp[i] = 1;
        }

        for (int i = 1; i < arr.length; i++) {
            int temp = 0;
            for (int j = 0; j < i; j++) {
                if(arr[i] > arr[j]){
                    temp = dp[j] + 1;
                    dp[i] = Math.max(dp[i], temp);
                }
            }
            maxLength = Math.max(maxLength, dp[i]);
        }
        return maxLength;
    }

    /**
     * 最长递增子序列的长度
     * @param A 数组
     * @param n 数组大小
     * @return
     */
    public int findLongest(int[] A, int n) {
        if(A == null || A.length == 0 || n <= 0) return 0;

        int maxLength = 1;
        int[] dp = new int[n];
        for (int i = 0; i < n; i++) {
            dp[i] = 1;
        }

        for (int i = 1; i < n; i++) {
            int temp = 0;
            for (int j = 0; j < i; j++) {
                if(A[i] >= A[j]){
                    temp = dp[j] + 1;
                    dp[i] = Math.max(dp[i], temp);
                }
            }
            maxLength = Math.max(maxLength, dp[i]);
        }
        return maxLength;
    }
}
