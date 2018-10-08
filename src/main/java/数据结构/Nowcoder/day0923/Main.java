package 数据结构.Nowcoder.day0923;

import java.util.Scanner;

/**
 * Created by Jay on 2017/9/23
 */
public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        while(sc.hasNextInt()){
            int year = sc.nextInt();
            int month = sc.nextInt();
            int day = sc.nextInt();
            int sum = 0;
            int leap = 0;
            switch (month){
                case 1:sum=0;
                    break;
                case 2:sum=31;
                    break;
                case 3:sum=59;
                    break;
                case 4:sum=90;
                    break;
                case 5:sum=120;
                    break;
                case 6:sum=151;
                    break;
                case 7:sum=181;
                    break;
                case 8:sum=212;
                    break;
                case 9:sum=243;
                    break;
                case 10:sum=273;
                    break;
                case 11:sum=304;
                    break;
                case 12:sum=334;
                    break;
                default:
                    System.out.println("data error");
            }
            sum += day;
            if(year % 400 == 0|| (year%4==0&&year%100!=0)){
                leap =1;
            }else {
                leap = 0;
            }
            if(leap==1&&month>2){
                sum++;
            }
            System.out.println(sum);
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
