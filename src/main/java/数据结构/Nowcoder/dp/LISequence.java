package 数据结构.Nowcoder.dp;

import java.util.Arrays;

/**
 * 求出最长递增子序列LIS 数组  O(N²)或者O(NlgN)
 */
public class LISequence {
    public static void main(String[] args) {
        int[] arr = {2,1,5,3,6,4,8,9,7};
        System.out.println(Arrays.toString(lis1(arr)));
        System.out.println(Arrays.toString(lis2(arr)));
    }

    public static int[] lis2(int[] arr){
        if(arr == null || arr.length == 0) return null;

        int[] dp = getdp2(arr); //得到dp数组 O(NlgN)
        return generateLIS(arr,  dp); //还原生成LIS
    }
    //计算dp数组，时间复杂度O(NlgN) 二分查找
    public static int[] getdp2(int[] arr){
        int[] dp = new int[arr.length]; //每个元素表示以每个位置结束的最长递增子序列长度
        dp[0] = 1; //初始化
        //ends[b]=c表示所有长度为b+1的递增序列中，最小的结尾数为c，递增
        int[] ends = new int[arr.length];
        ends[0] = arr[0];
        int right = 0; //有效区右边界

        int l = 0, r = 0, m = 0; //二分查找 l左，r右，m=(l+r)/2

        for (int i = 1; i < arr.length; i++) { //遍历
            l = 0; //不变
            r = right; //变化
            while(l <= r){ //二分查找
                m = (l+r)/2;
                if(arr[i] > ends[m]){
                    l = m + 1;
                }else {
                    r = m -1;
                }
            }
            right = Math.max(right, l); //S1可能更新边界
            ends[l] = arr[i]; //S2替换或者新增
            dp[i] = l + 1; //S3更新dp[i]
        }
        return dp;
    }
    //-----------------------------------------------------
    //-----------------------------------------------------
    public static int[] lis1(int[] arr){
        if(arr == null || arr.length == 0) return null;

        int[] dp = getdp1(arr); //得到dp数组
        return generateLIS(arr,  dp); //还原生成LIS
    }
    //dp数组，每个元素表示以每个位置结束的最长递增子序列长度 O(N²)
    public static int[] getdp1(int[] arr){
        int[] dp = new int[arr.length];

        for (int i = 0; i < arr.length; i++) {
            dp[i] = 1;
            for (int j = 0; j < i; j++) {
                if(arr[i] > arr[j]){
                    dp[i] = Math.max(dp[i], dp[j]+1);
                }
            }
        }
        return dp;
    }
    //根据状态数组dp，还原LIS
    public static int[] generateLIS(int[] arr, int[] dp){
        int len = dp[0]; //LIS长度
        int index = 0; //位置
        for (int i = 1; i < dp.length; i++) { //找到最长长度及位置
            if(dp[i] > len){
                len = dp[i];
                index = i;
            }
        }

        int[] lis = new int[len]; //LIS数组
        lis[--len] = arr[index]; //最后一个值

        for (int i = index; i >= 0; i--) { //从右往左扫描
            if(arr[index] > arr[i] && dp[index] == dp[i] + 1){
                lis[--len] = arr[i];
                index = i;
            }
        }
        return lis;
    }
}
