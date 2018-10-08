package 数据结构.Nowcoder.day826;

import java.util.Scanner;

/**
 * Created by Jay on 2017/8/26
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        String nums = sc.nextLine();
        String[] me = nums.split("\\s+");
        int[] arr = new int[me.length];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.parseInt(me[i]);
        }

        int k = sc.nextInt();

        System.out.println(num(arr, k));

    }

    public static int num(int[] arr, int k){
        if (arr == null || arr.length < k) {
            return 0;
        }
        sort(arr, 0, arr.length-1);
        return arr[arr.length-k];
    }

    public static void sort(int[] arr, int lo, int hi){
        if(hi <= lo) return;

        int part = partition(arr, lo, hi);

        sort(arr, lo, part-1);
        sort(arr, part + 1, hi);
    }

    public static int partition(int[] arr, int lo, int hi){
        int i = lo, j = hi + 1;
        int v = arr[lo];

        while(true){
            while(arr[++i] <= v){
                if( i == hi) break;
            }
            while(arr[--j] >= v){
                if( j == lo) break;
            }

            if(i >= j) break;

            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        int temp = arr[lo];
        arr[lo] = arr[j];
        arr[j] = temp;

        return j;
    }



}
