package 数据结构.Nowcoder.day0909;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by Jay on 2017/9/9
 */
public class Main2 {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = sc.nextInt();
        }
        int[] l = new int[n];
        int[] r = new int[n];

        for (int i = 0; i < n; i++) {
            l[i] = cal1(arr, i);
            r[i] = cal2(arr, i);
        }
        int c = Integer.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            c = Math.min(c, l[i]+ r[i]);
        }

        System.out.println(c);

    }

    public static int cal1(int[] arr, int i){
        int sum = 0;
        for (int j = 0; j < i-2; j++) {
            sum += arr[j+1]-arr[j];
        }
        return sum;
    }

    public static int cal2(int[] arr, int i){
        int sum = 0;
        for (int j = i; j < arr.length-1; j++) {
            sum += arr[j+1]-arr[j];
        }
        return sum;
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
