package 数据结构.Nowcoder.dp;

import java.util.Scanner;
//  http://songlee24.github.io/2014/11/27/dynamic-programming/
/**
 * 最长递增子序列长度求解
 *
        m+1
     0000000000
     0？？？？？
 n+1 0？
     0？
     0？
     0？
 */
public class LCSequence {
    /**
     * 最长递增子序列长度求解
     * @param A 字符串A
     * @param n A长度
     * @param B 字符串B
     * @param m B长度
     * @return 最长递增子序列长度求解
     */
    public static int findLCS(String A, int n, String B, int m) {
        if(A == null || n <= 0 || B == null || m <= 0) return 0;

        char[] a = A.toCharArray();
        char[] b = B.toCharArray();
        int[][] dp = new int[n+1][m+1]; //第一行，第一列为空，分别表示字符串长度为0

        for(int i=1; i<n+1; i++){
            for(int j = 1;j<m+1; j++) {
                if (a[i - 1] == b[j - 1]){ //a前i个字符，b前j个字符
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                }else {
                    //分别去掉其中一个字符的可能性
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        return dp[n][m]; //返回最大值
    }
    //计算其中一个最长公共序列
    public static String lcse(String A, String B){
        if(A == null || A.equals("") || B == null || B.equals("")) return "";

        char[] a = A.toCharArray();
        char[] b = B.toCharArray();
        int n = a.length;
        int m = b.length;
        int[][] dp = dp(a, b); //n+1,m+1 注意dp数组长度行n+1,列m+1

        char[] res = new char[dp[n][m]]; //最长公共子序列字符数组
        int index = res.length-1; //插入位置
        while(index >= 0){
            if(n >= 1 && dp[n][m] == dp[n-1][m]){ //向左  左/上都可以的情况
                n --;
            }else if(m >= 1 && dp[n][m] == dp[n][m-1]){ //向上
                m --;
            }else if(n >= 1 && m >= 1 && dp[n][m] == dp[n-1][m-1] + 1){ //斜向左上, a[n-1]=b[m-1]
                res[index--] = a[n-1];
                n --;
                m --;
            }
        }
        return String.valueOf(res);
    }

    public static int[][] dp(char[] a, char[] b) {
        int n = a.length;
        int m = b.length;
        int[][] dp = new int[n+1][m+1]; //第一行，第一列为空，分别表示字符串长度为0

        for(int i=1; i<n+1; i++){
            for(int j = 1;j<m+1; j++) { //三者取最大值
                //分别去掉其中一个字符的可能性
                dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                if (a[i - 1] == b[j - 1]){ //a、b最后一个字符相等
                    dp[i][j] = Math.max(dp[i][j], dp[i - 1][j - 1] + 1);
                }
            }
        }
        return dp; //返回最大值
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNext()){
            String A = sc.next();
            String B = sc.next();
            System.out.println(findLCS(A, A.length(), B, B.length()));
            int[][] dp = dp(A.toCharArray(), B.toCharArray());
            for (int i = 0; i < dp.length; i++) {
                for (int j = 0; j < dp[0].length; j++) {
                    System.out.print(dp[i][j] + " ");
                }
                System.out.println();
            }
            System.out.println(lcse(A, B));
        }
    }
}
