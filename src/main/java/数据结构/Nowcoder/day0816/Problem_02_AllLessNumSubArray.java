package 数据结构.Nowcoder.day0816;

import java.util.LinkedList;

/**
 *  求满足条件的子数组个数 max(a[i,j]) -min(a[i,j]) <= num
 */
public class Problem_02_AllLessNumSubArray {

    public static int getNum(int[] arr, int num) {
        if (arr == null || arr.length == 0) {
            return 0;
        }
        LinkedList<Integer> qmin = new LinkedList<Integer>(); //窗口内最小值更新双端队列,左小右大递增
        LinkedList<Integer> qmax = new LinkedList<Integer>(); //窗口内最大值更新双端队列,左大右小递减
        int i = 0; //左指针
        int j = 0; //右指针
        int res = 0;
        while (i < arr.length) {
            while (j < arr.length) { //r右移更新
                while (!qmin.isEmpty() && arr[qmin.peekLast()] >= arr[j]) { //min更新
                    qmin.pollLast();
                }
                qmin.addLast(j);
                while (!qmax.isEmpty() && arr[qmax.peekLast()] <= arr[j]) { //max更新
                    qmax.pollLast();
                }
                qmax.addLast(j);
                if (arr[qmax.getFirst()] - arr[qmin.getFirst()] > num) { //最大-最小
                    break;
                }
                j++;
            }
            if (qmin.peekFirst() == i) { //l右移更新
                qmin.pollFirst();
            }
            if (qmax.peekFirst() == i) {
                qmax.pollFirst();
            }
            res += j - i; //结算以i位置开头的子数组个数
            i++;
        }
        return res;
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

    public static void main(String[] args) {
        int[] arr = getRandomArray(30);
        int num = 5;
        printArray(arr);
        System.out.println(getNum(arr, num));

    }

}
