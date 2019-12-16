package 数据结构.sort;

import java.util.Arrays;

public class Sort {

    // 归并排序
    public static void merge_sort(int[] nums) {
        if (nums == null || nums.length <= 1) return;

        int[] aux = new int[nums.length];
        merge_sort(nums, 0, nums.length-1, aux);
    }
    private static void merge_sort(int[] nums, int lo, int hi, int[] aux) {
        if (lo >= hi) return;
        int mid = (lo + hi) / 2;
        merge_sort(nums, lo, mid, aux);
        merge_sort(nums, mid+1, hi, aux);

        int i = lo, j = mid+1;

        for(int k=lo;k<=hi;k++) {
            aux[k] = nums[k];
        }

        int k = lo;
        while ( i<= mid && j<=hi) {
            if (aux[i] <= aux[j]) {
                nums[k++]= aux[i++];
            } else {
                nums[k++]=aux[j++];
            }
        }
        if (i<=mid) {
            while(i<=mid) {
                nums[k++]=aux[i++];
            }
        }
        if (j<=hi) {
            while(j<=hi) {
                nums[k++]=aux[j++];
            }
        }
    }

    // 快排
    public static void quick_sort(int[] nums) {
        if (nums == null || nums.length <= 1) return;

        quick_sort(nums, 0, nums.length-1);
    }
    private static void quick_sort(int[] nums, int lo, int hi) {
        if(lo >= hi) return;

        int j = partition(nums, lo, hi);
        quick_sort(nums, lo, j-1);
        quick_sort(nums, j+1, hi);
    }
    private static int partition(int[] nums, int lo, int hi) {
        int i=lo, j=hi+1;

        int v = nums[lo];
        while(true) {
            while (nums[++i] <= v) if(i == hi) break;
            while(nums[--j] >= v) if(j == lo) break;
            if(i >= j) break;
            swap(nums, i, j);
        }
        swap(nums, lo, j);
        return j;
    }

    // 堆排序
    public static void heap_sort(int[] nums) {
        if (nums == null || nums.length <= 1) return;

        int[] newArr = new int[nums.length+1];
        System.arraycopy(nums, 0, newArr, 1, nums.length);

        int N = newArr.length-1;
        for (int k = N/2; k >= 1; k--) {
            sink(newArr, k, N); // 构建大顶堆
        }

        while (N > 1) {
            swap(newArr, 1, N--);
            sink(newArr, 1, N); // 排序
        }

        System.arraycopy(newArr, 1, nums, 0, nums.length);
    }
    private static void sink(int[] nums, int k, int N) {
        while(2*k <= N) {
            int j = 2*k;
            if(j < N && nums[j] < nums[j+1]) j++;
            if(nums[k] >= nums[j]) break;
            swap(nums, k, j);
            k = j;
        }
    }

    private static void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    public static void main(String[] args) {
        int[] nums = {4, 3, -1, 8, -10, -5, 15};
//        merge_sort(nums);
//        quick_sort(nums);
        heap_sort(nums);
        System.out.println(Arrays.toString(nums));
    }

}
