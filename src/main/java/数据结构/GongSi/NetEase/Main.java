package 数据结构.GongSi.NetEase;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int x = sc.nextInt(); //日租

        int f = sc.nextInt(); //已有f个水果

        int d = sc.nextInt(); //有多少钱

        int p = sc.nextInt(); //商店水果价格每个

        int count = 0; //天数

        while(true){
            if(f > 0){
                f --;
            }else {
                if(f == 0) {
                    if (d >= p) {
                        d -= p;
                    } else {
                        break;
                    }
                }
            }
            if(d >= x){
                d -= x;
            }else {
                break;
            }
            count ++;
        }
        System.out.println(count);

        /*if(d/x < f){
            System.out.println(d/x);
        }else {
            System.out.println(live(x, f, d, p));
        }*/
    }

    public static int live(int x, int f, int d, int p){

        double temp = (p*f+d)*1.0/(x+p);

        return (int)Math.floor(temp);
    }

    /*public static void main(String[] args) {
        System.out.println(live(3,5,100,10));
    }*/

}
