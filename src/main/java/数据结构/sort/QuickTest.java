package 数据结构.sort;

import java.util.Arrays;

/**
 * desc.
 *
 * @author zhongshuo.xwj
 */
public class QuickTest {

    public static void quick(int[] nums) {
        if (nums == null || nums.length <= 1) {
            return;
        }
        quick(nums, 0, nums.length-1);
    }

    private static void quick(int[] nums, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int j = partition(nums, lo, hi);
        quick(nums, 0, j-1);
        quick(nums, j+1, hi);
    }

    private static int partition(int[] nums, int lo, int hi) {
        int i = lo, j = hi+1;
        int v = nums[lo];
        while (true) {
            while(nums[++i] <= v) if (i==hi) break;
            while(nums[--j] >= v) if (j==lo) break;
            if (i >= j) break;
            swap(nums, i, j);
        }
        swap(nums, lo, j);
        return j;
    }

    private static void swap(int[] nums, int i, int j) {
        int tmp = nums[i];
        nums[i] = nums[j];
        nums[j] = tmp;
    }

    public static void main(String[] args) {
        int[] nums = {4, 3, -1, 8, -10, -5, 15};
        quick(nums);
        System.out.println(Arrays.toString(nums));
    }

}
