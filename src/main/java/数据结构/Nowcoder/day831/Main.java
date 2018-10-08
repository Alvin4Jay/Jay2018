package 数据结构.Nowcoder.day831;

import java.util.Scanner;

/**
 * Created by Jay on 2017/8/31
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int N = sc.nextInt();

        int[] arr = new int[N];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = sc.nextInt();
        }

        int K = sc.nextInt();

        System.out.println(cal3(arr, K));

    }

    public static int cal3(int[] arr, int k){
        int max = 0;
        for (int i = 0; i < arr.length; i++) {
            int sum = 0;
            for (int j = i; j < arr.length; j++) {
                sum += arr[i];
                if(sum % k == 0){
                    max = Math.max(max, j - i +1);
                }
            }
        }
        return max;
    }

    public static int cal2(int[] arr, int k){
        int[] dp = new int[arr.length];
        int max = 0;
        for (int i = arr.length -1; i >= 0; i--) {
            dp[i] = arr[i] % k;
            if(dp[i] == 0){
                max += 1;
            }
            for (int j = i+1; j < arr.length; j++) {
                dp[i] = (dp[j] +arr[j])%k;
                if(dp[j] == 0){
                    max += 1;
                }
            }
        }
        return max;
    }


    public static int cal(int[] arr, int k){
        int maxLen = 0;

        int temp = 0;
        for (int l = 1; l <= arr.length; l++) {
            for (int i = 0; i < arr.length - l+1; i++) {
                int j = i + l - 1;
                temp = sum(arr, i, j);
                if(temp%k == 0){
                    maxLen = Math.max(maxLen, j-i+1);
                }

            }
        }
        return maxLen;
    }

    public static int sum(int[] arr, int i, int j){
        int sum = 0;
        for (int k = i; k <= j; k++) {
            sum += arr[k];
        }
        return sum;
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
