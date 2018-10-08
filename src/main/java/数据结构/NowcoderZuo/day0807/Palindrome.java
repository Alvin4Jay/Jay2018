package 数据结构.NowcoderZuo.day0807;

/**
 * 给定一个字符串str，返回str中最长回文子串的长度
 */
public class Palindrome {
    //O(N³) 暴力求解
    public int getLongestPalindrome(String A, int n) {
        if(A == null || n <= 0) return 0;

        int maxL = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j <= n; j++) {
                if(isHuiWen(A.substring(i, j), j-i)){
                    maxL = Math.max(maxL, j-i);
                }
            }
        }
        return maxL;
    }
    public boolean isHuiWen(String s, int length){
        int k = length / 2;
        for (int i = 0; i < k; i++) {
            if(s.charAt(i) != s.charAt(length - i-1)){
                return false;
            }
        }
        return true;
    }

    //动态规划O(n²)
    /*

    动态规划
    1.dp[i][j]表示，若i到j已经是回文串了，那么dp[i][j]是回文串的长度，否则为0；
    2.初始时dp[i][i]=1，i=0,2,3...n-1；
    3.递归公式，len>2时，当dp[i+1][j-1]!=0,且a[i]==a[j]时，dp[i][j] = 2+dp[i+1][j-1],
    否则dp[i][j]=0，i<j。这是一个从已知回文串往两边展开的过程。当len=2时，特殊处理一下，因为当len=2时，
    dp[i+1][j-1]会访问到dp矩阵的左下方，我们没对那个位置做初始化处理，因此不能直接用递推公式
     */
    public int getLongestPalindrome2(String A, int n) {
        if(A == null || n <= 0) return 0;

        int maxL = 1;
        //dp[i][j]表示i到j是否为回文串,且长度是多少，o表示不是回文串，大于0为回文串
        int[][] dp = new int[n][n];
        for (int i = 0; i < n; i++) {
            dp[i][i] = 1;
        }

        for(int len = 2; len <= n; len ++){  //回文串长度枚举
            for (int i = 0; i <= n-len; i++) { //定长度子串枚举
                int j = i + len -1; //j位置
                if(len == 2 && A.charAt(i) == A.charAt(j)){ //len=2是特殊情况
                    dp[i][j] = 2;
                    maxL = 2;//回文串，去掉左右两边还是回文串
                }else if(len > 2 && A.charAt(i) == A.charAt(j) && dp[i+1][j-1] != 0){
                    dp[i][j] = len;
                    maxL = len;
                }
            }
        }
        return maxL;
    }

    public String longestPalindrome(String s) {
        if(s == null || s.length() <= 0) return s;

        int maxL = 1;
        //dp[i][j]表示i到j是否为回文串,且长度是多少，o表示不是回文串，大于0为回文串
        int[][] dp = new int[s.length()][s.length()];
        for (int i = 0; i < s.length(); i++) {
            dp[i][i] = 1;
        }

        int l = 0;
        int r = 0;

        for(int len = 2; len <= s.length(); len ++){  //回文串长度枚举
            for (int i = 0; i <= s.length()-len; i++) { //定长度子串枚举
                int j = i + len -1; //j位置
                if(len == 2 && s.charAt(i) == s.charAt(j)){ //len=2是特殊情况
                    dp[i][j] = 2;
                    if(j-i+1 > maxL){
                        maxL = j-i+1;
                        l = i;
                        r = j;
                    }
                }else if(len > 2 && s.charAt(i) == s.charAt(j) && dp[i+1][j-1] != 0){
                    dp[i][j] = len;
                    if(j-i+1 > maxL){
                        maxL = j-i+1;
                        l = i;
                        r = j;
                    }
                }
            }
        }
        return s.substring(l, r+1);
    }

    public static void main(String[] args) {
        System.out.println(new Palindrome().getLongestPalindrome("baabccc", 6));
        System.out.println(new Palindrome().getLongestPalindrome2("baabccc", 6));
    }
}
