package 数据结构.Nowcoder.day0913;

import java.util.Scanner;

/**
 * Created by Jay on 2017/9/13
 */
public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int a = sc.nextInt();
        int b = sc.nextInt();
        int n = sc.nextInt();

        int count = 0;
        int min =  a*b/gcd(a, b);
        int sum = min;
        while(sum <= n){
            count ++;
            sum += min ;
        }
        System.out.println(count);
    }

    public static int gcd(int a, int b){
        while(true){
            if((a=a%b) == 0){
                return b;
            }
            if((b=b%a) == 0){
                return a;
            }
        }
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
