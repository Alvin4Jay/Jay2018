package 数据结构.LeetCode.Array;

/**
 Given an array nums, write a function to move all 0's to the end of it
 while maintaining the relative order of the non-zero elements.

 For example, given nums = [0, 1, 0, 3, 12], after calling your function,
 nums should be [1, 3, 12, 0, 0].

 Note:
 You must do this in-place without making a copy of the array.
 Minimize the total number of operations.
 Credits:
 Special thanks to @jianchao.li.fighter for adding this problem and creating all test cases.
 */
//双指针
public class Ex283_Move_Zeroes {
    public void moveZeroes(int[] nums) {
        if(nums == null || nums.length == 0) return;

        int insertPos = 0; //移动指针
        for(int num : nums){ //增强for循环
            if(num != 0) nums[insertPos++] = num; //>0的数前插
        }

        while(insertPos < nums.length){
            nums[insertPos ++] = 0; //后面的补0
        }
    }
}
