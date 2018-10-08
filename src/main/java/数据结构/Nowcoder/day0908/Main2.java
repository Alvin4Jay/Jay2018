package 数据结构.Nowcoder.day0908;

import java.util.Scanner;

/**
 * Created by Jay on 2017/9/8
 */
public class Main2 {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        String str  = in.nextLine();
        long res = 1;
        int size = str.length();
        int count = 0;
        for (int i = size-1; i >= 0; i--) {
            if(str.charAt(i)==')'){
                count ++;
            }else if(str.charAt(i)=='('){
                res = (res * count);
                count --;
            }
        }
        System.out.println(res);
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
