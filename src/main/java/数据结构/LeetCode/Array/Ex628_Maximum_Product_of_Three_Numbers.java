package 数据结构.LeetCode.Array;

/**
 Given an integer array, find three numbers whose product is maximum and output the maximum product.

 Example 1:
 Input: [1,2,3]
 Output: 6
 Example 2:
 Input: [1,2,3,4]
 Output: 24
 Note:
 The length of the given array will be in range [3,104] and all elements are in the range [-1000, 1000].
 Multiplication of any three numbers in the input won't exceed the range of 32-bit signed integer.
 */
//取其中的最大三个数， 最大的一个数与最小的两个数比较
public class Ex628_Maximum_Product_of_Three_Numbers {
    public int maximumProduct(int[] nums) {
        /*Arrays.sort(nums);
        int n = nums.length;
        return Math.max(nums[n-1] * nums[n-2] * nums[n-3], nums[n-1] * nums[0] * nums[1]); */

        int max1=Integer.MIN_VALUE, max2=Integer.MIN_VALUE, max3=Integer.MIN_VALUE,
                min1=Integer.MAX_VALUE, min2=Integer.MAX_VALUE;

        for(int i=0;i<nums.length;i++){
            int t = nums[i];
            if(t > max1){
                max3 = max2;
                max2 = max1;
                max1 = t;
            }else if(t > max2){
                max3 = max2;
                max2 = t;
            }else if(t > max3){
                max3 = t;
            }


            if(t < min1){
                min2 = min1;
                min1 = t;
            }else if(t < min2){
                min2 = t;
            }
        }
        return Math.max(max1*max2*max3, max1*min1*min2);
    }
}
