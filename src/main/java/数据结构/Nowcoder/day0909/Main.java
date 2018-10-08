package 数据结构.Nowcoder.day0909;

import java.util.Scanner;

/**
 * Created by Jay on 2017/9/9
 */
public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt();

        int c1 = 0, c2=0, c3=0, len=0;
        int j ;

        for (int i = 0; i < n; i++) {
            len = sc.nextInt();
            c1 = 0;c2=0;c3=0;
            for ( j = 0; j < len; j++) {
                int num = sc.nextInt();
                if(num%4 == 0){
                    c3 ++;
                }else if(num%2==0){
                    c2++;
                }else {
                    c1++;
                }
            }
            if(c1 > c3+1){
                System.out.println("No");
            }else if(c2==1&&c1==c3+1){
                System.out.println("No");
            }else {
                System.out.println("Yes");
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
