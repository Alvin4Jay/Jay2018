package 数据结构.Nowcoder.day0916;

import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by Jay on 2017/9/16
 */
public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt();

        Point[] points = new Point[n];


        for (int i = 0; i < n; i++) {
            Point point = new Point();
            point.x = sc.nextInt();
            point.y = sc.nextInt();
            points[i] = point;
        }


        System.out.println(cal(points));


    }

    private static int cal(Point[] points){
        int m = points.length;
        if(m == 0){
            return 0;
        }

        if(m < 2) return m;
        int max = 0;

        HashMap<Double, Integer> map = new HashMap<>();
        for (int i = 0; i < m; i++) {
            map.clear();
            int dup = 0;
            for (int j = i+1; j < m; j++) {
                if(points[i].x == points[j].x &&points[i].y == points[j].y ){
                    dup ++;
                    continue;
                }
                double k = points[i].x == points[j].x ? Integer.MAX_VALUE :
                        (0.0+points[i].y-points[j].y)/(points[i].x-points[j].x)*1.0;

                if(map.containsKey(k)){
                    map.put(k, map.get(k)+1);
                }else {
                    map.put(k, 2);
                }
            }
            if(map.size() == 0){
                max = Math.max(max, dup+1);
            }else {
                for (int tmp : map.values()) {
                    if(tmp+dup > max){
                        max = tmp+dup;
                    }
                }
            }
        }
        return max;
    }

    private static class Point {
        int x;
        int y;
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
