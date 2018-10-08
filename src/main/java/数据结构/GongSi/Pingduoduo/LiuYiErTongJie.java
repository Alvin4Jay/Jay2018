package 数据结构.GongSi.Pingduoduo;

import java.util.Arrays;
import java.util.Scanner;

/**
 * 分糖果
 */
public class LiuYiErTongJie {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] h = new int[n];
        for (int i = 0; i < h.length; i++) {
            h[i] = sc.nextInt();
        }
        int m = sc.nextInt();
        int[] w = new int[m];
        for (int i = 0; i < w.length; i++) {
            w[i] = sc.nextInt();
        }

        System.out.println(num(h, w));
    }

    public static int num(int[] h, int[] w){
        Arrays.sort(h);
        Arrays.sort(w);

        int hp = 0;
        int wp = 0;
        while(hp < h.length && wp < w.length){
            if(h[hp] <= w[wp]){
                hp ++;
                wp ++;
            }else {
                wp ++;
            }
        }
        return hp;
    }
}
