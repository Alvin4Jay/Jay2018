package 数据结构.Nowcoder.day726;

import java.util.Scanner;

/**
 * 子矩阵的最大累加和
 */
public class MaxSumOfSubMatrix {
    public static void main(String[] args){
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextInt()){
            int N = sc.nextInt();
            int[][] m = new int[N][N];
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    m[i][j] = sc.nextInt();
                }
            }

            int maxSum = maxSum(m);
            System.out.println(maxSum);
        }
    }
    //子矩阵的最大累加和
    public static int maxSum(int[][] m){
        if(m == null || m.length == 0 || m[0].length == 0) return 0;

        int M = m.length; //行
        int N = m[0].length; //列
        int maxSum = Integer.MIN_VALUE; //最大和
        int cur = 0; //记录局部最大
        int[] sums = null; //累加和数组

        for (int i = 0; i < M; i++) { //行
            sums = new int[N];
            for (int j = i; j < M; j++) { //行
                cur = 0;
                for (int k = 0; k < N; k++) {
                    sums[k] += m[j][k];
                }
                for (int k = 0; k < N; k++) { //一次累加计算局部最大
                    if(cur < 0){
                        cur = sums[k];
                    }else {
                        cur += sums[k];
                    }
                    maxSum = Math.max(cur, maxSum);
                }
            }
        }
        return maxSum;
    }
}
