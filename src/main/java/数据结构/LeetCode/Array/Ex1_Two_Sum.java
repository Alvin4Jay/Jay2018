package 数据结构.LeetCode.Array;

import java.util.HashMap;
import java.util.Map;

/**
 Given an array of integers, return indices of the two numbers such that they add up to a specific target.

 You may assume that each input would have exactly one solution, and you may not use the same element twice.

Given nums = [2, 7, 11, 15], target = 9,

 Because nums[0] + nums[1] = 2 + 7 = 9,
 return [0, 1].

 */
public class Ex1_Two_Sum {
    //O(n)时间复杂度，O(n)空间复杂度
    public int[] twoSum(int[] nums, int target) {
        int[] result = new int[2];
        Map<Integer, Integer> map = new HashMap<Integer, Integer>(); //数组值为键，数组索引为值

        for (int i = 0; i < nums.length; i++) {
            if(map.containsKey(target-nums[i])){
                result[0] = map.get(target-nums[i]);
                result[1] = i;
                return result;
            }
            map.put(nums[i], i);
        }
        return result;
    }

    //暴力搜索 O(n²)
    /*int[] index = new int[2];

    int size = nums.length;
    for(int i=0 ;i < size; i++){
        for(int j=i+1; j< size; j++){
            if(nums[i] + nums[j] == target){
                index[0] = i;
                index[1] = j;
                return index;
            }
        }
    }
    return null;*/

}
