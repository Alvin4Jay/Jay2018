package 数据结构.Nowcoder.dp;

import java.util.Scanner;

/**
 * 矩阵的最小路径和（左上到右下）
 输入
 4 4
 1 3 5 9
 8 1 3 4
 5 0 6 1
 8 8 4 0

 输出
 12
 */
public class MinPathInMatrix {

    //动态规划空间压缩 O(MxN)  O(min{M, N})
    public static int getMinPathAwesome(int[][] m){
        if(m == null || m.length == 0 || m[0] == null ||m[0].length == 0) return 0;

        int more = Math.max(m.length, m[0].length); //行 列中较大的
        int less = Math.min(m.length, m[0].length); //行 列中较小的
        boolean rowMore = (more == m.length); //是否是行大于列

        //初始化
        int[] dp = new int[less]; //状态矩阵，dp数组长度为较小的那个
        dp[0] = m[0][0];
        for (int i = 1; i < less; i++) { //初始化
            dp[i] = dp[i-1] + (rowMore?  m[0][i]: m[i][0]);
        }
        //计算，滚动更新
        for (int i = 1; i < more; i++) {
            dp[0] += rowMore? m[i][0] : m[0][i];
            for (int j = 1; j < less; j++) {
                dp[j] = Math.min(dp[j-1], dp[j]) + (rowMore? m[i][j] : m[j][i]);
            }
        }
        return dp[less-1];
    }

    //二维dp数组全部计算，浪费空间 O(MxN) O(MxN)
     public static int getMinPath(int[][] m){
        if(m == null || m.length == 0 || m[0] == null ||m[0].length == 0) return 0;

        int M = m.length;
        int N = m[0].length;

        int[][] dp = new int[M][N];
        dp[0][0] = m[0][0];

        for (int i = 1; i < M; i++) {
            dp[0][i] = dp[0][i-1] + m[0][i];
        }
        for (int i = 1; i < N; i++) {
            dp[i][0] = dp[i-1][0] + m[i][0];
        }

        for (int i = 1; i < M; i++) {
            for (int j = 1; j < N; j++) {
                dp[i][j] = Math.min(dp[i][j-1], dp[i-1][j]) + m[i][j];
            }
        }
        return dp[M-1][N-1];
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextInt()){
            int M = sc.nextInt();
            int N = sc.nextInt();
            int[][] m = new int[M][N];
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {
                    m[i][j] = sc.nextInt();
                }
            }
            System.out.println(getMinPath(m) == getMinPathAwesome(m));
        }
    }
}
