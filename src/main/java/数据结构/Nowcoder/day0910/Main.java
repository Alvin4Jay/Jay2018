package 数据结构.Nowcoder.day0910;

import java.util.Scanner;

/**
 * Created by Jay on 2017/9/10
 */
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int N = sc.nextInt();
        System.out.println(getUgly(N));
    }

    public static  int getUgly(int index){
        if(index <= 0) return 0;

        int[] buf = new int[index];
        buf[0] =1;
        int s1 = 0;
        int s2 = 0;
        int s3 = 0;
        for (int i = 1; i < index; i++) {
            buf[i] = Math.min(buf[s1]*2, Math.min(buf[s2]*3, buf[s3]*5));
            if(buf[i] == buf[s1]*2) s1++;
            if(buf[i] == buf[s2]*3) s2++;
            if(buf[i] == buf[s3]*5) s3++;

        }
        return buf[index-1];
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
