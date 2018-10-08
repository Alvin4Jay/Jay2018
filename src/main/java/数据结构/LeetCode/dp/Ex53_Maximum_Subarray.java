package 数据结构.LeetCode.dp;

/**
 Find the contiguous subarray within an array (containing at least one number) which has the largest sum.

 For example, given the array [-2,1,-3,4,-1,2,1,-5,4],
 the contiguous subarray [4,-1,2,1] has the largest sum = 6.
 */
//最大子数组之和
public class Ex53_Maximum_Subarray {
    public int maxSubArray(int[] nums) {
        int sum  = Integer.MIN_VALUE;
        int cur = 0;
        for(int i=0;i<nums.length;i++){
            cur += nums[i];
            sum = Math.max(sum, cur);
            cur = cur < 0 ? 0 : cur;
        }
        return sum;
    }
}
