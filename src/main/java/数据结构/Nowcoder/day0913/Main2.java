package 数据结构.Nowcoder.day0913;

import java.util.Scanner;

/**
 * Created by Jay on 2017/9/13
 */
public class Main2 {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int m = sc.nextInt();

        int[] arr = new int[m];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = sc.nextInt();
        }

        int res = win1(arr);
        if(res > 0){
            System.out.println("True");
        }else if(res < 0 ){
            System.out.println("false");
        }

    }

    //决策(暴力递归)
    public static int win1(int[] arr) {
        if (arr == null || arr.length == 0) {
            return 0;
        }
        int A = f(arr, 0, arr.length - 1);
        int B = s(arr, 0, arr.length - 1);
        return A - B;
    }
    //作为先发者，最终获得的最优分数(收益)
    /**
     * 求先发者最终的分数
     * @param arr 卡片
     * @param i 左边界
     * @param j 右边界
     * @return 先发者最终的分数
     */
    public static int f(int[] arr, int i, int j) {
        if (i == j) {  //只有一个数
            return arr[i];
        }
        if(i +1 == j) return arr[i] + arr[j]; //2个
        return Math.max(arr[i] + s(arr, i + 1, j), arr[i] +arr[i+1] + s(arr, i+2, j));
    }
    //作为后发者，最终获得的最优分数(收益)
    /**
     * 求后发者最终的最优分数
     * @param arr 卡片
     * @param i 左边界
     * @param j 右边界
     * @return 后发者最终的最优分数
     */
    public static int s(int[] arr, int i, int j) {
        if (i == j || i + 1 == j ) {  //只有一个数，返回0
            return 0;
        }
        return Math.min(f(arr, i + 1, j), f(arr, i+2, j)); //后发者只能拿到相对小的那个分数
    }
}
