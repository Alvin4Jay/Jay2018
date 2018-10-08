package Algorithms.String.SubstringSearch;

/**
 * 暴力搜索 时间复杂度O(nm),n为S的长度，m为P的长度
 */
public class BruteForceSearch {
    public static int search(String S, String P){
        int s_len = S.length(); //文本长度
        int p_len = P.length(); //模式字符串长度
        int i=0; //S的下标
        int j=0; //P的下标

        while(i < s_len && j < p_len){
            if(S.charAt(i) == P.charAt(j)){ //若相等则都前进一步
                i ++; //文本匹配到的位置
                j ++;
            }else { //不相等，i回退
                i = i-j +1;
                j = 0;
            }
        }
        if(j == p_len) return i-j;
        else return -1;
    }
}
