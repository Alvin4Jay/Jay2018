package 数据结构.Nowcoder.day726;

/**
 * 连续子数组的最大和
 */
public class GreatestSumOfSubarrays {
    public int FindGreatestSumOfSubArray(int[] array) {
        if(array == null || array.length <= 0) return 0;

        int cur = 0; //一轮中的和
        int total = Integer.MIN_VALUE; //遍历中总的连续子数组最大和

        for(int i =0;i < array.length; i++){
            if(cur < 0){
                cur = array[i]; //新一轮
            }else{
                cur += array[i];
            }
            if(cur > total){
                total = cur;
            }
        }
        return total;
    }
    //dp
    public int FindGreatestSumOfSubArray2(int[] array){
        if(array == null || array.length <= 0) return 0;

        int max = array[0]; //连续子数组的最大累加和
        int[] dp = new int[array.length]; //以每个位置结尾的最大和
        dp[0] = array[0];
        for (int i = 1; i < array.length; i++) {
            if(dp[i-1] < 0){
                dp[i] = array[i];
            }else {
                dp[i] = dp[i-1] + array[i];
            }
            max = Math.max(max, dp[i]);
        }
        return max;
    }

    public static void main(String[] args) {
//        int[] a = new int[]{1,-2,3,10,-4,7,2,-5};
//        int[] a = new int[]{1,-2,3,5,-2,6,-1};
        int[] a = new int[]{-2,-8,-1,-5,-9};
        System.out.println(new GreatestSumOfSubarrays().FindGreatestSumOfSubArray(a));
    }
}
