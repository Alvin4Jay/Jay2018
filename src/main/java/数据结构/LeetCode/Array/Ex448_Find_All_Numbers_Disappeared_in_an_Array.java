package 数据结构.LeetCode.Array;

import java.util.ArrayList;
import java.util.List;

/**
 Given an array of integers where 1 ≤ a[i] ≤ n (n = size of array), some elements appear twice and others appear once.

 Find all the elements of [1, n] inclusive that do not appear in this array.

 Could you do it without extra space and in O(n) runtime? You may assume the returned list does not count as extra space.

 Example:

 Input:
 [4,3,2,7,8,2,3,1]

 Output:
 [5,6]
 */
public class Ex448_Find_All_Numbers_Disappeared_in_an_Array {
    //O(n)时间复杂度， O(1)空间复杂度
    public List<Integer> findDisappearedNumbers(int[] nums) {
        List<Integer> result = new ArrayList<Integer>();

        for (int i = 0; i < nums.length; i++) {
            int val = Math.abs(nums[1]) - 1; //val表示nums[i]实际索引位置
            if(nums[val] > 0){   //1--n中未存在nums[i]，标记存在；为负，说明已存在
                nums[val] = -nums[val];
            }
        }

        for (int i = 0; i < nums.length; i++) {
            if(nums[i] > 0) result.add(i+1);
        }
        return result;
    }
}
