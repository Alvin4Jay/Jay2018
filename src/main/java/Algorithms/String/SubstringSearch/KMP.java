package Algorithms.String.SubstringSearch;

/**
 *  KMP子字符串查找
 */
public class KMP {
    /* P为模式串，下标从0开始 */
    private static void GetNext(String P, int next[]){
        int p_len = P.length();
        int i = 0;   //P的下标
        int j = -1;  //i位置之前的子字符串相同的前后缀字符个数
        next[0] = -1; //规定

        while (i < p_len){
            if (j == -1 || P.charAt(i) == P.charAt(j)){
                i++;
                j++;
                next[i] = j;
            }else {
                j = next[j]; //递归查找更短的重复的前后缀
            }
        }
    }

    /* 在S中找到P第一次出现的位置 */
    public static int KMP(String S, String P){
        int[] next = new int[S.length()];
        GetNext(P, next);

        int i = 0;  //S的下标
        int j = 0;  //P的下标
        int s_len = S.length();
        int p_len = P.length();

        while (i < s_len && j < p_len){
            if (j == -1 || S.charAt(i) == P.charAt(j)){  //P的第一个字符不匹配或S[i] == P[j]
                i++;
                j++;
            }else {
                j = next[j];  //当前字符匹配失败，进行跳转
            }
        }

        if (j == p_len)  //匹配成功
            return i - j;

        return -1; //匹配失败
    }

    public static void main(String[] args) {
        String S = "bbc abcdab abcdabcdabde";
        String P = "abcdabd";
        int pos = KMP(S, P);
        System.out.println(pos);
    }
}
