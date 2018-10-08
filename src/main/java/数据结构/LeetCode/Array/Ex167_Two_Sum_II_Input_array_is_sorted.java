package 数据结构.LeetCode.Array;

/**
 Given an array of integers that is already sorted in ascending order, find two numbers
 such that they add up to a specific target number.

 The function twoSum should return indices of the two numbers such that they add up to the
 target, where index1 must be less than index2. Please note that your returned answers (both index1 and index2) are not zero-based.

 You may assume that each input would have exactly one solution and you may not use the same element twice.

 Input: numbers={2, 7, 11, 15}, target=9
 Output: index1=1, index2=2
 */
public class Ex167_Two_Sum_II_Input_array_is_sorted {
    public int[] twoSum(int[] numbers, int target) {

        //采用HashMap O(n) O(n)
        /*int[] result = new int[2];
        Map<Integer, Integer> m = new HashMap<Integer, Integer>();
        for(int i=0;i<numbers.length;i++){
            if(m.containsKey(target-numbers[i])){
                result[0] = m.get(target-numbers[i]);
                result[1] = i+1;
                return result;
            }

            m.put(numbers[i], i+1);
        }
        return result;*/

        //采用双指针做法 O(n) O(1)
        int[] result = new int[2];

        if(numbers == null || numbers.length < 2) return result;

        int left = 0, right = numbers.length-1;
        while(left < right){
            int val = numbers[left] + numbers[right];
            if(val == target){
                result[0] = left + 1;
                result[1] = right + 1;
                return result;
            }else if(val > target){
                right --;
            }else{
                left ++;
            }
        }
        return result;
    }

}
