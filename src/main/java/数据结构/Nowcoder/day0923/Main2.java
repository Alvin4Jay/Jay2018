package 数据结构.Nowcoder.day0923;

import java.util.Scanner;

/**
 * Created by Jay on 2017/9/23
 */
public class Main2 {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        


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
