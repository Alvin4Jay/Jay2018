package 数据结构.LeetCode.Array;

public class Ex53_Maximum_Subarray {
    public int maxSubArray(int[] nums) {
        if(nums == null) return 0;

        int max = Integer.MIN_VALUE;
        int cur  = 0;

        for (int i = 0; i < nums.length; i++) {
            cur += nums[i];
            max = Math.max(max, cur);
            cur = cur>0 ? cur : 0 ;
        }
        return max;
    }
}
