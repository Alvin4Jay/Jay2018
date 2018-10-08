package 数据结构.LeetCode.dp;

/**
 Given an integer array nums, find the sum of the elements between indices i and j (i ≤ j), inclusive.

 Example:
 Given nums = [-2, 0, 3, -5, 2, -1]

 sumRange(0, 2) -> 1
 sumRange(2, 5) -> -1
 sumRange(0, 5) -> -3
 Note:
 You may assume that the array does not change.
 There are many calls to sumRange function.

 */
public class Ex303_Range_Sum_Query {
    private static int[] sum;

    public Ex303_Range_Sum_Query(int[] nums) {
        for (int i = 1; i < nums.length; ++i)
            nums[i] += nums[i - 1];
        this.sum = nums;
    }

    public int sumRange(int i, int j) {
        return sum[j] - (i == 0 ? 0 : sum[i - 1]);
    }
}
