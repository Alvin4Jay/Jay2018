package 数据结构.LeetCode.Array;

/**
 Given an array consisting of n integers, find the contiguous subarray of given length k that has the maximum
 average value. And you need to output the maximum average value.

 Example 1:
 Input: [1,12,-5,-6,50,3], k = 4
 Output: 12.75
 Explanation: Maximum average is (12-5-6+50)/4 = 51/4 = 12.75
 Note:
 1 <= k <= n <= 30,000.
 Elements of the given array will be in the range [-10,000, 10,000].

 */
public class Ex643_Maximum_Average_Subarray_I {
    public double findMaxAverage(int[] nums, int k) {
        if(nums == null || nums.length == 0 || k > nums.length) return 0;

        int sum = Integer.MIN_VALUE;
        for(int i=0;i < nums.length-k+1 ; i++){
            int temp = 0;
            for(int j = i; j < k+i; j++){
                temp += nums[j];
            }
            if(temp > sum){
                sum = temp;
            }
        }
        return 1.0*sum/k;
    }
}
