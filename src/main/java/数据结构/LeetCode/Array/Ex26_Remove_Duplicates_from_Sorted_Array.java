package 数据结构.LeetCode.Array;

import java.util.Arrays;
import java.util.HashSet;

/**
 Given a sorted array, remove the duplicates in place such that each element appear only once and
 return the new length.

 Do not allocate extra space for another array, you must do this in place with constant memory.

 For example,
 Given input array nums = [1,1,2],

 Your function should return length = 2, with the first two elements of nums being 1 and 2 respectively. It doesn't
 matter what you leave beyond the new length.
 */
public class Ex26_Remove_Duplicates_from_Sorted_Array {
    public  static  int removeDuplicates(int[] nums) {
        if(nums == null || nums.length == 0) return 0;

        if(nums.length == 1) return 1;

        int left = 0;
        for (int right = 1; right < nums.length; right++) {

            if(nums[left] != nums[right]){
                nums[++left] = nums[right];
            }
        }

        return left + 1;
    }

    public  static  int removeDuplicates2(int[] nums) {
        HashSet<Integer> set = new HashSet<>();
        for (int i = 0; i < nums.length; i++) {
            set.add(nums[i]);
        }

        return set.size();
    }

    public static int[] generatePositiveArray(int size) {
        int[] result = new int[size];
        for (int i = 0; i != size; i++) {
            result[i] = (int) (Math.random() * 15) + 1;
        }
        return result;
    }

    public static void main(String[] args) {
        boolean flag = true;
        for (int i = 0; i < 20000; i++) {
            int[] arr = generatePositiveArray(10);
            Arrays.sort(arr);
            if(removeDuplicates(arr) != removeDuplicates2(arr)){
                flag = false;
                break;
            }
        }
        if(flag){
            System.out.println("666666");
        }else {
            System.out.println("233333");
        }
    }
}
