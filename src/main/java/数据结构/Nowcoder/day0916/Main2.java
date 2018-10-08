package 数据结构.Nowcoder.day0916;

import java.util.*;

/**
 * Created by Jay on 2017/9/16
 */
public class Main2 {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        String[] arr = sc.nextLine().split("\\s+");

        Comparator<String> cmp = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String s1 = o1+o2;
                String s2 = o2+o1;
                return s2.compareTo(s1);
            }
        };

        Arrays.sort(arr,  cmp);

        if(arr[0].charAt(0) == '0') {
            System.out.println("0");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(s);
        }
        System.out.println(sb.toString());
    }



    private static int min(int i, int j){
        if(i > j) {
            return j;
        }else {
            return i;
        }
    }


}
