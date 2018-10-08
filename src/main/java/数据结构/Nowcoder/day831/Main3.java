package 数据结构.Nowcoder.day831;

import java.util.Scanner;

/**
 * Created by Jay on 2017/8/31
 */
public class Main3 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);




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
