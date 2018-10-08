package 数据结构.Nowcoder.day0910;

import java.util.Scanner;

/**
 * Created by Jay on 2017/9/10
 */
public class Main2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt();

        int[] arr = new int[n];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = sc.nextInt();
        }

        System.out.println(cal(arr));

    }

    public static int cal(int[] arr){

        int source = 0;
        int res = 0;

        for (int i = 0; i < arr.length; i++) {
            if(arr[i] == 0){
                res ++;
                source = i + 1;
                continue;
            }
            for (int j = source; j < i; j++) {
                int token = 0;
                for (int k = j; k <= i; k++) {
                    token = token ^ arr[k];
                    if(token == 0){
                        res ++;
                        source = i+1;
                    }
                }
            }
        }

        return res;
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
