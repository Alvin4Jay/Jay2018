package 数据结构.NowcoderZuo.day0807;

/**
 * Manacher算法求最长回文子串O(N)
 */
public class Manacher1 {

    public static char[] getCharArr(String s){
        char[] oldArr = s.toCharArray();
        char[] newArr = new char[oldArr.length*2 + 1];
        int index = 0;
        for (int i = 0; i < newArr.length; i++) {
            if(i % 2 == 0){
                newArr[i] = '#';
            }else {
                newArr[i] = oldArr[index++];
            }
        }
        return newArr;
    }

    public static int getLongestHuiWen(String s){
        if(s == null || s.length() <= 0) return 0;

        char[] charArr = getCharArr(s);
        int[] pArr = new int[charArr.length]; //处理后的字符串回文半径数组

        int pR = -1;
        int index = -1;
        int max = Integer.MIN_VALUE; //处理后的字符串最长回文子串半径

        for (int i = 0; i < pArr.length; i++) {
            pArr[i] = pR > i ? Math.min(pArr[2*index - i], pR-i) : 1;
            while(i + pArr[i] < charArr.length && i - pArr[i] >= 0){
                if(charArr[i+pArr[i]] == charArr[i-pArr[i]]){
                    pArr[i] ++;
                }else {
                    break;
                }
            }
            if(i+pArr[i] > pR){
                pR = i+pArr[i];
                index = i;
            }
            max = Math.max(max, pArr[i]);
        }
        return max - 1; //原字符串的最长回文长度
    }

    public static void main(String[] args) {
        String str1 = "abc1234321ab";
        System.out.println(getLongestHuiWen(str1));

    }

}
