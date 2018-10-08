package 数据结构.Nowcoder.day0910_2;

import java.util.Scanner;
import java.util.Stack;

/**
 * Created by Jay on 2017/9/10
 */
public class Main2 {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        String str = sc.next().trim();
        char[] a = str.toCharArray();
        int len  =a.length;

        /*int len = str.length();
        int max = 0, count = 0;

        for (int i = 0; i < len; i++) {
            if(a[i] == '('){
                count ++;
                max = Math.max(max, count);
            }else if(a[i] == ')'){
                count = 0;
            }
        }

        System.out.println(max);*/

        Stack<Character> stack = new Stack<>();
        int max = 0, count=0;
        for (int i = 0; i < len; i++) {
            if(a[i] == '('){
                stack.push(a[i]);
                count ++;
            }else if(a[i] == ')'){
                stack.pop();
                count --;
            }
            max = Math.max(max,count);
        }
        System.out.println(max);
    }




    // for test
    public static int[] getRandomArray(int len) {
        if (len < 0) {
            return null;
        }
        int[] arr = new int[len];
        for (int i = 0; i < len; i++) {
            arr[i] = (int) (Math.random() * 10);
        }
        return arr;
    }

    // for test
    public static void printArray(int[] arr) {
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                System.out.print(arr[i] + " ");
            }
            System.out.println();
        }
    }

}
