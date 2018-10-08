package 数据结构.GongSi.Pingduoduo;

import java.util.Scanner;

/**
 * 大数相乘
 */
@Deprecated
public class BigDataMulti {

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
    //非负数相乘
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

    public static void main(String[] args) {
        /*System.out.println(multi("123", "456"));
        System.out.println(multi("123", "-456"));
        System.out.println(multi("-123", "456"));
        System.out.println(multi("-123", "-456"));*/

        Scanner sc = new Scanner(System.in);
        String s1  =sc.next();
        String s2  =sc.next();
        System.out.println(multi(s1, s2));

    }
}
