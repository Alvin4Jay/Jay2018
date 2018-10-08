package 数据结构.Nowcoder.dp;

/**
 * 最长公共子串 长度及子串
        m+1
     0000000000
     0？？？？？
 n+1 0？
     0？
     0？
     0？
 */
public class LCSubstring {
    //计算最长公共子串长度
    public static int findLongest(String A, int n, String B, int m) {
        if(A == null || n <= 0 || B == null || m <= 0) return 0;

        char[] a = A.toCharArray();
        char[] b = B.toCharArray();
        int[][] dp = new int[n+1][m+1]; //以i,j位置结尾的最长公共子串长度
        int maxLength = 0; //全局最长

        for(int i=1; i<n+1; i++){
            for(int j = 1;j<m+1; j++){
                if(a[i-1] == b[j-1]) //a[i-1] b[j-1]对应的dp[i][j]
                    dp[i][j] = dp[i-1][j-1] + 1;
                else
                    dp[i][j] = 0;
                maxLength = Math.max(maxLength, dp[i][j]);
            }
        }
        return maxLength;
    }

    //返回公共子串  空间复杂度O(1)
    public static String lcst2(String A, String B){
        if(A == null || A.equals("") || B == null || B.equals("")) return "";

        char[] a = A.toCharArray();
        char[] b = B.toCharArray();

        int row = 0;  //斜线开始的行
        int col = b.length - 1; //斜线开始的列
        int max = 0;  //LCSu最大值
        int end = 0; //LCSu字符串末尾位置

        while(row < a.length){ //斜线还没遍历完
            int i = row; //一条斜线开始行
            int j = col; //一条斜线开始列
            int len = 0; //一条斜线中的目前长度
            while(i < a.length && j < b.length){ //一条斜线还没遍历到右边界
                if(a[i] == b[j]){ //相等
                    len ++;
                }else {
                    len = 0; //不相等，为0
                }
                if(len > max){ //每次更新
                    max = len;
                    end = i;
                }
                i ++; //当前点往右下移动
                j ++;
            }
            if(col > 0){ //斜线开始位置的列先向左移动
                col --;
            }else { //列移动到最左之后，行往下移动
                row ++;
            }
        }

        return A.substring(end - max + 1, end + 1);
    }

    //返回公共子串 dp S  O(MxN)
    public static String lcst(String A, String B){
        if(A == null || A.equals("") || B == null || B.equals("")) return "";

        char[] a = A.toCharArray();
        char[] b = B.toCharArray();

        int[][] dp = dp(A, B); //n+1  m+1

        int maxLength = 0;
        int end = 0;

        for (int i = 1; i < dp.length; i++) {
            for (int j = 1; j < dp[0].length; j++) {
                if(dp[i][j] > maxLength){
                    maxLength = dp[i][j];
                    end = i-1;
                }
            }
        }

        return A.substring(end - maxLength + 1, end + 1);
    }

    public static int[][] dp(String A, String B) {
        char[] a = A.toCharArray();
        char[] b = B.toCharArray();
        int n = a.length;
        int m = b.length;
        int[][] dp = new int[n+1][m+1]; //以i,j位置结尾的最长公共子串长度

        for(int i=1; i<n+1; i++){
            for(int j = 1;j<m+1; j++){
                if(a[i-1] == b[j-1]) //a[i-1] b[j-1]对应的dp[i][j]
                    dp[i][j] = dp[i-1][j-1] + 1;
                else
                    dp[i][j] = 0;
            }
        }
        return dp;
    }

    public static void main(String[] args) {
        System.out.println(findLongest("1AB2345CD",9,"12345EF",7));
        /*int[][] dp = dp("1AB2345CD","12345EF");
        for (int i = 0; i < dp.length; i++) {
            for (int j = 0; j < dp[0].length; j++) {
                System.out.print(dp[i][j] + " ");
            }
            System.out.println();
        }*/
        System.out.println(lcst("1AB2345CD","12345EF"));
        System.out.println(lcst2("1AB2345CD","12345EF"));
    }
}
