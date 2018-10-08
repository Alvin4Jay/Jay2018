package 数据结构.LeetCode.Array;

/**

 Rotate an array of n elements to the right by k steps.

 For example, with n = 7 and k = 3, the array [1,2,3,4,5,6,7] is rotated to
 [5,6,7,1,2,3,4].

 Note:
 Try to come up as many solutions as you can, there are at least 3 different ways
 to solve this problem.

 [show hint]

 Hint:
 Could you do it in-place with O(1) extra space?
 Related problem: Reverse Words in a String II

 Credits:
 Special thanks to @Freezen for adding this problem and creating all test cases.

三次反转数组：
     第一次反转整个数组；
     第二次反转数组的前K个数；
     第三次反转数组剩下的数
 */
public class Ex189_Rotate_Array {
    //O(n)时间  O(n)空间
    public void rotate(int[] nums, int k) {
        k = k % nums.length; //取余

        int[] result = new int[nums.length];

        for(int i=0; i < k; i++){
            result[i] = nums[nums.length-k+i];
        }

        int j=0;
        for(int i=k; i<nums.length; i++){
            result[i] = nums[j];
            j++;
        }

        System.arraycopy( result, 0, nums, 0, nums.length ); //src to dest
    }
}
