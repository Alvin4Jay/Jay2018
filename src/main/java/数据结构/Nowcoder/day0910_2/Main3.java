package 数据结构.Nowcoder.day0910_2;

import java.util.Scanner;

/**
 * Created by Jay on 2017/9/10
 */
public class Main3 {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt();
        int m = sc.nextInt();
        int res  =0;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if(isInt(i,j)) res ++;
            }
        }
        System.out.println(res);
    }

    public static boolean isInt(int i, int j){
        double res = Math.sqrt(1.0*i*j);
        return (int) res - res == 0.0;
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
