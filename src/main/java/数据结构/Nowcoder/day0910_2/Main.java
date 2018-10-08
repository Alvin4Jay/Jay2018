package 数据结构.Nowcoder.day0910_2;

import java.util.Scanner;

/**
 * Created by Jay on 2017/9/10
 */
public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);


        int  x1 = sc.nextInt();
        int k1 = sc.nextInt();
        int  x2 = sc.nextInt();
        int k2 = sc.nextInt();

        StringBuilder s1 = new StringBuilder(x1);
        StringBuilder s2 = new StringBuilder(x1);
        for (int i = 0; i < k1; i++) {
            s1.append(x1);
        }
        for (int i = 0; i < k2; i++) {
            s2.append(x2);
        }

        String c1 = s1.toString().trim();
        String c2 = s2.toString().trim();

        if(c1.length()>c2.length()){
            System.out.println("Greater");
        }else if(c1.length() < c2.length()){
            System.out.println("Less");
        }else {
            int res = c1.compareTo(c2);
            if(res <0 ){
                System.out.println("Less");
            }else if(res == 0){
                System.out.println("Equal");
            }else {
                System.out.println("Greater");
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
