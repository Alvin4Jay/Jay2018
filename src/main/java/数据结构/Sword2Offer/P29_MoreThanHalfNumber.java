package 数据结构.Sword2Offer;

import java.util.HashMap;
import java.util.Map;

/**
 * 数组中出现次数超过一半的数字
 */
public class P29_MoreThanHalfNumber {
    //1.HashMap O(n) O(n)
    public int MoreThanHalfNum_Solution1(int [] array) {
        if(array == null || array.length == 0) return 0;

        Map<Integer, Integer> map = new HashMap<Integer, Integer>();

        for (int i : array) {
            if(!map.containsKey(i)){
                map.put(i, 1);
            }else {
                map.put(i, map.get(i)+1);
            }
        }
        for (int i : map.keySet()) {
            if(map.get(i) > array.length/2){
                return i;
            }
        }
        return 0; //不存在，返回0
    }

    //2.利用数组特点 O(n) O(1) 次数最多的数比其他数加起来的次数还要多
    public int MoreThanHalfNum_Solution2(int [] array) {
        if(array == null || array.length == 0) return 0;

        int result = array[0];
        int time = 1;

        for (int i = 1; i < array.length; i++) {
            if(time == 0){
                result = array[i];
                time = 1; //最后把次数设置为1的数就是次数最多的数
            }else if(array[i] == result){
                time ++;
            }else{
                time --;
            }
        }

        int count = 0;
        for (int i = 0; i < array.length; i++) {
            if(array[i] == result){
                count ++;
            }
        }
        if(count <= array.length/2){ //不存在
            return 0;
        }

        return result;
    }

    //O(n)利用快排切分的思想
    public int MoreThanHalfNum_Solution3(int [] array) {
        if(array == null || array.length == 0) return 0;

        int N = array.length;
        int lo = 0, hi = N - 1;
        int index = partition(array, lo, hi); //索引
        while(index != N/2){ //知道index为中位数
            if(index > N/2){
                hi = index-1;
                index = partition(array, lo, hi);
            }else if(index < N/2){
                lo = index+1;
                index = partition(array, lo, hi);
            }
        }

        int time = 0;
        for (int i = 0; i < array.length; i++) {
            if(array[i] == array[index]){
                time ++;
            }
        }
        if(time <= N/2){ //不存在
            return 0;
        }

        return array[index];
    }
    //快排切分,返回切分位置
    public int partition(int[] array, int lo, int hi){
        if(lo == hi) return lo;

        int i = lo, j = hi + 1;

        int v = array[lo];

        while(true){
            while(array[++i]<v)
                if(i == hi) break;
            while(array[--j]>v)
                if(j == lo) break;
            if(i >= j){
                break;
            }
            swap(array, i, j);
        }
        swap(array, lo, j);
        return j;
    }

    public void swap(int[] array, int i, int j){
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
