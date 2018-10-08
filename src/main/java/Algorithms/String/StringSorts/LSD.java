package Algorithms.String.StringSorts;

/**
 * 低位优先的字符串排序 LSD
 */
public class LSD {
    public static void sort(String[] a, int W){ //W次键索引计数排序
        int N = a.length;
        int R = 256; //进制
        String[] aux = new String[N]; //辅助数组

        for (int d = W-1; d >= 0; d--) {//从字符串末尾开始键索引计数排序
            int[] count = new int[R+1];
            for (int i = 0; i < N; i++) { //计算出现频率
                count[a[i].charAt(d) + 1] ++;
            }

            for (int r = 0; r < R; r++) { //将频率转换为起始索引
                count[r+1] += count[r];
            }

            for (int i = 0; i < N; i++) {  //元素分类，相对位置不变
                aux[count[a[i].charAt(d)] ++] = a[i];
            }

            for (int i = 0; i < N; i++) { //回写
                a[i] = aux[i];
            }
        }

    }
}
