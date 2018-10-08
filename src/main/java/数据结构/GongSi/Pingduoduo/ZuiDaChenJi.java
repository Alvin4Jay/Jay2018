package 数据结构.GongSi.Pingduoduo;

import java.util.Scanner;

/**
 * 三最大   一最大二最小
 */
public class ZuiDaChenJi {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int M = sc.nextInt();
        int[] a = new int[M];
        for (int i = 0; i < a.length; i++) {
            a[i] = sc.nextInt();
        }
        System.out.println(max(a));
    }

    //考虑可能溢出
    public static long max(int[] a){
        long max1 = Integer.MIN_VALUE;
        long max2 = Integer.MIN_VALUE;
        long max3 = Integer.MIN_VALUE;
        long min1 = Integer.MAX_VALUE;
        long min2 = Integer.MAX_VALUE;

        for (int i = 0; i < a.length; i++) {
            if(a[i] > max1){
                max3 = max2;
                max2 = max1;
                max1 = a[i];
            }else if(a[i] > max2){
                max3 = max2;
                max2 = a[i];
            }else if(a[i] > max3){
                max3 = a[i];
            }

            if(a[i] < min1){
                min2 = min1;
                min1 = a[i];
            }else if(a[i] < min2){
                min2 = a[i];
            }
        }
        return Math.max(max1*max2*max3, max1*min1*min2);
    }
}
