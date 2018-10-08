package 数据结构.GongSi.Pingduoduo;

/**
 * https://segmentfault.com/a/1190000003890059#articleHeader6
 */
@Deprecated
public class BigDataSub {
    //无符号数相减 O(N) O(N)
    public static String subString(String s1, String s2){
        int len1 = s1.length(), len2 = s2.length();

        if(len1 > len2){
            return coreSub(s1, s2);
        }else if(len2 > len1){
            return "-" + coreSub(s2, s1);
        }else {
            int com = s1.compareTo(s2);
            if(com > 0) return coreSub(s1, s2);
            if(com < 0) return "-" + coreSub(s2, s1);
            else return "0";
        }
    }
    //无符号数大减小
    private static String coreSub(String s1, String s2){
        int i = s1.length()-1, j = s2.length()-1;

        int[] res = new int[i+1]; //结果最多为大的数s1的长度
        int borrow = 0; //是否借位
        while(i >= 0){
            int a1 = s1.charAt(i) - '0';
            int a2 = j>=0 ? (s2.charAt(j) -'0') : 0;

            int result = a1 - a2 - borrow + 10; //先借10，后面如果结果>=10，表示不用借位，否则需要借位
            res[i] = result % 10;
            borrow = result>=10 ? 0 : 1; //》=10。表示没借位

            i --;
            j --;
        }
        int index = 0;
        while(res[index] == 0){ //取出前面的0
            index ++;
        }
        StringBuilder sb = new StringBuilder();
        for (int l = index; l < res.length; l++) {
            sb.append(res[l]);
        }
        return sb.toString();
    }

    //有符号相减 O(N) O(N)
    public static String subSignedString(String s1, String s2){
        int sign1 = 1, sign2  =1;
        if(s1.charAt(0) == '-'){
            sign1 = -1;
            s1 = s1.substring(1);
        }
        if(s2.charAt(0) == '-'){
            sign2 = -1;
            s2 = s2.substring(1);
        }
        String s1_ex = s1.substring(1, s1.length());
        String s2_ex = s2.substring(1, s2.length());
        if(sign1 == sign2 ){
            if(sign1 == 1){
                return subString(s1, s2); //都是正数
            }else {
                return subString(s2, s1); //都是负数
            }
        }else {
            if(sign1 == 1){
                return BigData.addString(s1, s2); //前正后负
            }else {
                return "-" +  BigData.addString(s1, s2); //前负后正
            }
        }
    }

    public static void main(String[] args) {
        //无符号相减
        System.out.println(subString("1111", "33"));

        //有符号相减
        System.out.println(subSignedString("1111", "-889"));
        System.out.println(subSignedString("-1111", "889"));
        System.out.println(subSignedString("-1111", "-111"));
        System.out.println(subSignedString("1111", "111"));
    }
}
