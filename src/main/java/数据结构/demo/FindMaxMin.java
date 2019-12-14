package 数据结构.demo;

import java.util.Arrays;

/**
 * 思路：用两个变量分别记录最大值、最小值，遍历数组，依次比较，O(n)时间复杂度即可得到最大值、最小值。
 */
public class FindMaxMin {
    /**
     * 数组中查找最大值、最小值
     *
     * @param nums 数组
     * @return [最大值，最小值] 或 []
     */
    public static int[] findMaxMin(int[] nums) {
        if (nums == null || nums.length <= 3) {
            return new int[]{};
        }

        int max = nums[0], min = nums[0];

        for(int i = 1; i< nums.length; i++) {
            max = Math.max(max, nums[i]);
            min = Math.min(min, nums[i]);
        }
        return new int[]{max, min};
    }

    public static void main(String[] args) {
        int[] nums = {-5, -2, -10 , 1, -3, 5, 15};

        int[] ans = findMaxMin(nums);

        System.out.println(Arrays.toString(ans));
    }

}
