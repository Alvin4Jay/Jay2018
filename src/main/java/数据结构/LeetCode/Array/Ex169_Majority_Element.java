package 数据结构.LeetCode.Array;

import java.util.HashMap;
import java.util.Set;

/**
 Given an array of size n, find the majority element. The majority
 element is the element that appears more than ⌊ n/2 ⌋ times.

 You may assume that the array is non-empty and the majority element
 always exist in the array.
 */
public class Ex169_Majority_Element {
    public int majorityElement(int[] nums) {
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for(int i=0; i< nums.length; i++){
            if(!map.containsKey(nums[i])){
                map.put(nums[i], 1);
            }else{
                int cnt = map.get(nums[i]);
                cnt ++;
                map.put(nums[i], cnt);
            }
        }
        Set<Integer> set = map.keySet();
        int ret = 0;
        for(int n : set){
            if(map.get(n) > nums.length/2){
                ret = n;
            }
        }
        return ret;
    }
}
