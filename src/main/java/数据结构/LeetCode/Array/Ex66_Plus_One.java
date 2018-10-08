package 数据结构.LeetCode.Array;

import java.util.Arrays;

/**
 *
 Given a non-negative integer represented as a non-empty array of digits, plus one to the integer.

 You may assume the integer do not contain any leading zero, except the number 0 itself.

 The digits are stored such that the most significant digit is at the head of the list.
 给定一个非负整数，表示非空数组，+1。

 您可以假设整数不包含任何前导零，除了数字0本身。

 存储数字使得最高有效数字位于列表的头部。

 模拟大数相加
 */
public class Ex66_Plus_One {
    public int[] plusOne(int[] digits) {

        int t = 0;
        digits[digits.length-1] += 1;
        for(int i=digits.length-1; i>=0; i--){

            int temp = digits[i] + t;
            if(temp >= 10){
                t = 1;
            }else{
                t = 0;
            }
            digits[i] = temp % 10;
        }
        //最高位有进位
        if(t == 1){
            int[] res = new int[digits.length +1];
            res[0] = t;
            for (int i = 1; i < res.length; i++) {
                res[i] = digits[i-1];
            }
            return res;
        }else{
            return digits;
        }
    }

    public static void main(String[] args) {
        int[] res = new Ex66_Plus_One().plusOne(new int[]{9,0,9});
        System.out.println(Arrays.toString(res));
    }
}
