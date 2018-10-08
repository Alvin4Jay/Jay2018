package 数据结构.Sword2Offer;

import java.util.*;

/**
 * 最小的k个数
 */
public class P30_KLeastNumbers {
    //快排O(NlgN)
    public ArrayList<Integer> GetLeastNumbers_Solution(int [] input, int k) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if(input == null || input.length == 0 || input.length < k || k == 0) return result;

        Arrays.sort(input);

        for (int i = 0; i < k; i++) {
            result.add(input[i]);
        }

        return result;
    }

    //优先队列 O(Nlgk) O(k)
    public ArrayList<Integer> GetLeastNumbers_Solution2(int [] input, int k) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if(input == null || input.length == 0 || input.length < k || k == 0) return result;

        PriorityQueue<Integer> pq = new PriorityQueue<>(k, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        });

        for (int i = 0; i < input.length; i++) {
            if(pq.size() < k){
                pq.add(input[i]); //未满，加入
            }else {
                if(pq.peek() > input[i]){
                    pq.poll();  //删除最大元素
                    pq.add(input[i]); //加入新元素
                }//否则不变
            }
        }
        result.addAll(pq);
        Collections.sort(result);
        return result;
    }

    //快排切分 O(N)
    public ArrayList<Integer> GetLeastNumbers_Solution3(int [] input, int k) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if(input == null || input.length == 0 || input.length < k || k == 0) return result;

        int start = 0, end = input.length-1;
        int index = partition(input, start, end);
        while(index != k-1){
            if(index > k-1){
                end = index-1;
                index = partition(input, start, end); //缩小范围
            }else {
                start = index+1;
                index = partition(input, start, end);
            }
        }
        for (int i = 0; i < k; i++) { //乱序
            result.add(input[i]);
        }
        return result;
    }

    //快排切分,返回切分位置
    public int partition(int[] input, int lo, int hi){
        if(lo == hi) return lo;

        int i = lo, j = hi + 1;

        int v = input[lo];

        while(true){
            while(input[++i]<v)
                if(i == hi) break;
            while(input[--j]>v)
                if(j == lo) break;
            if(i >= j){
                break;
            }
            swap(input, i, j);
        }
        swap(input, lo, j);
        return j;
    }

    public void swap(int[] input, int i, int j){
        int temp = input[i];
        input[i] = input[j];
        input[j] = temp;
    }
}
