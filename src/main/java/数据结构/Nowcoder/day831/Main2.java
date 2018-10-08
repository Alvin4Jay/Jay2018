package 数据结构.Nowcoder.day831;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by Jay on 2017/8/31
 */
public class Main2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = sc.nextInt();
        }

        cal(arr, n);

    }


    public static void cal(int[] arr, int len){
        Arrays.sort(arr);
        int sum=0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        if(sum <= arr[len-1]){
            System.out.println("No");
        }else{
                System.out.println("Yes");
            }
    }




    public static int[] generatePositiveArray(int size) {
        int[] result = new int[size];
        for (int i = 0; i != size; i++) {
            result[i] = (int) (Math.random() * 10) + 1;
        }
        return result;
    }

    public static void printArray(int[] arr) {
        for (int i = 0; i != arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();
    }
}
