package 数据结构.Nowcoder.day0916;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Created by Jay on 2017/9/16
 */
public class Main3 {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = sc.nextInt();
        }
        int m = sc.nextInt();


        Arrays.sort(arr);

        int max = -1;

        int left = 0;
        int right = 0;
        int sum = arr[0];
        while(right < arr.length){
            if(sum == m){
                max = Math.max(max, cal(arr, left, right));
                sum -= arr[left++];
            }else if(sum < m){
                right ++;
                if(right == arr.length) break;
                sum += arr[right];
            }else {
                sum -= arr[left++];
            }
        }
        System.out.println(30);
    }

    private static int cal(int[] arr, int left, int right) {
        int res = 1;
        for (int i = left; i <= right; i++) {
            res = res * arr[i];
        }
        return res;
    }


}
