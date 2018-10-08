package 数据结构.Nowcoder.day92;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

/**
 * Created by Jay on 2017/9/2
 */
public class Main {
    public static void main(String[] args) {
        Scanner  sc = new Scanner(System.in);


        int n = sc.nextInt();

        int num = 0;

        Integer[] p = null;

        for (int i = 0; i < n; i++) {
            num = sc.nextInt();
            p = new Integer[3];
            p[0] = sc.nextInt();
            p[1] = sc.nextInt();
            p[2] = sc.nextInt();
            System.out.println(cal(num, p));
        }
    }

    public static int cal(int num, Integer[] p){
        Arrays.sort(p, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        char[] arr = String.valueOf(num).toCharArray();
        int[] temp = new int[arr.length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = Integer.parseInt(String.valueOf(arr[i]));
        }
        int n1 = 0, n2 = 0;

        while(n1 < temp.length && n2 < p.length){
            if(temp[n1] < p[n2]){
                temp[n1] = p[n2];
                n1 ++;
                n2 ++;
            }else {
                n2 ++;
            }
        }
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < temp.length; i++) {
            res.append(temp[i]);
        }
        return Integer.parseInt(res.toString());
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