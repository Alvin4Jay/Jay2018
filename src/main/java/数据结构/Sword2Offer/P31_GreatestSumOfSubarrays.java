package 数据结构.Sword2Offer;

/**
 * 连续子数组的最大和
 */
public class P31_GreatestSumOfSubarrays {
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

    public int FindGreatestSumOfSubArray2(int[] array){
        if(array == null || array.length <= 0) return 0;

        int max = array[0];
        int[] dp = new int[array.length];
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
        System.out.println(new P31_GreatestSumOfSubarrays().FindGreatestSumOfSubArray(a));
    }
}
