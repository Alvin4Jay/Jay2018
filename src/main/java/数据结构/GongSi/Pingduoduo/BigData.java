package 数据结构.GongSi.Pingduoduo;

/**
 * 大数运算(相加，相减，相乘)
 */
public class BigData {

    //无符号数相加1
    public static String addNonNegative(String s1, String s2){
        if(s1 == null && s2 == null ) return "0";
        if(s1 == null || s1.length() == 0){
            return s2;
        }
        if(s2 == null || s2.length() == 0){
            return s1;
        }

        String n1 = new StringBuilder(s1).reverse().toString();
        String n2 = new StringBuilder(s2).reverse().toString();
        int len1 = n1.length();
        int len2 = n2.length();
        int len = len1 >= len2 ? len1 : len2;
        if(len1 >= len2){
            for (int i = len2; i < len1; i++) {
                n2 += "0";
            }
        }else {
            for (int i = len1; i < len2; i++) {
                n1 += "0";
            }
        }
        char[] chas1 = n1.toCharArray();
        char[] chas2 = n2.toCharArray();

        boolean flag  = false; //最高位相加是否溢出
        int over = 0; //每次相加是否溢出
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; i++) {
            int res = Integer.parseInt(chas1[i]+"") + Integer.parseInt(chas2[i]+"") + over;

            if(res >= 10){
                over = 1;
                sb.append(res - 10);
                if(i == len-1){
                    flag = true;
                }
            }else {
                sb.append(res);
            }
        }

        if(flag){
            sb.append(1);
        }
        return sb.reverse().toString();
    }
    //无符号数相加2  O(N) O(N)
    public static String addString(String s1, String s2){
        int i = s1.length()-1, j = s2.length()-1;
        int[] res = new int[Math.max(i, j) + 1]; //除去最高位进位的数组

        int over = 0; //每次进位与否
        int k = res.length-1; //从k开始相加
        while(i >= 0 || j  >= 0){
            int a1 = i >= 0? (s1.charAt(i)-'0') : 0;
            int a2 = j >= 0? (s2.charAt(j)-'0') : 0;
            int sum = a1 + a2 + over;
            res[k] = sum%10; //取余
            over = sum/10;  //取整

            k --;
            i --;
            j --;
        }
        StringBuilder sb = new StringBuilder();
        if(over == 1){
            sb.append(1);
        }
        for (int re : res) {
            sb.append(re);
        }
        return sb.toString();
    }

    //有符号数相加 O(N) O(N)
    public static String addSignedString(String s1, String s2){
        int sign1 = 1, sign2 = 1;
        if(s1.charAt(0) == '-'){
            sign1 = -1;
            s1 = s1.substring(1);
        }
        if(s2.charAt(0) == '-'){
            sign2 = -1;
            s2 = s2.substring(1);
        }
        if(sign1 == sign2){
            if(sign1 == 1){
                return addString(s1, s2); //都正 无符号相加
            }else {
                return "-" + addString(s1, s2); //都负 无符号相加
            }
        }else {
            if(sign1 == 1){
                return subString(s1, s2); //无符号相减
            }else{
                return subString(s2, s1); //无符号相减
            }
        }
    }

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
        while(res[index] == 0){ //去除前面的0
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

        if(sign1 == sign2 ){
            if(sign1 == 1){
                return subString(s1, s2); //都是正数
            }else {
                return subString(s2, s1); //都是负数
            }
        }else {
            if(sign1 == 1){
                return addString(s1, s2); //前正后负
            }else {
                return "-" +  addString(s1, s2); //前负后正
            }
        }
    }

    //大数相乘，考虑正负数
    public static String multi(String s1, String s2){
        int sign1 = 1, sign2 = 1;
        if(s1.charAt(0) == '-'){ //负数
            sign1 = -1;
            s1 = s1.substring(1);
        }
        if(s2.charAt(0) == '-'){ //负数
            sign2 = -1;
            s2 = s2.substring(1);
        }
        if(sign1 == sign2){ //都正，或者都负
            return multiply(s1, s2);
        }else {
            return "-" + multiply(s1, s2); //一正一负
        }
    }
    //非负数相乘 O(MN) O(N+M)
    private static String multiply(String s1, String s2){
        int len1 = s1.length();
        int len2 = s2.length();
        //结果的位数最多可能是两个乘数位数之和
        int[] res = new int[len1 + len2]; //结果最多为len1+len2位数
        int carry = 0, i = 0, j = 0; //carry表示进位
        for (i = len1-1; i >= 0; i--) {
            carry = 0;  ///每开始一轮，先置carry位为0
            for (j = len2-1; j >= 0; j--) {
                // 每一位的乘积，等于之前这一位的结果，加上carry，再加上这一位和乘数的乘积
                int temp = (s1.charAt(i) - '0')*(s2.charAt(j) - '0') + carry + res[i+j+1];
                res[i+j+1] = temp % 10;
                carry = temp / 10;
            }
            res[i+j+1] = carry; //最后一位相乘后可能进位
        }
        i = 0;
        while(i < res.length-1 && res[i] == 0){ //结果可能为0， 保留最后一位0输出
            i ++;
        }

        StringBuilder sb = new StringBuilder();
        for( ; i< res.length; i++){
            sb.append(res[i]);
        }
        return sb.toString();
    }

    public static void main(String[] args){
        //无符号相加
        System.out.println(addNonNegative("9911","22"));
        System.out.println(addString("9911","22"));

        //有符号相加
        System.out.println(addSignedString("-999999999999999999", "1"));
        System.out.println(addSignedString("-2132", "-44"));

        System.out.println("------------------------------------------------");
        //无符号相减
        System.out.println(subString("1111", "33"));

        //有符号相减
        System.out.println(subSignedString("1111", "-889"));
        System.out.println(subSignedString("-1111", "889"));
        System.out.println(subSignedString("-1111", "-111"));
        System.out.println(subSignedString("1111", "111"));

        System.out.println("------------------------------------------------");
        //大叔相乘
        System.out.println(multi("123", "456"));
        System.out.println(multi("123", "-456"));
        System.out.println(multi("-123", "456"));
        System.out.println(multi("-123", "-456"));
    }

}
