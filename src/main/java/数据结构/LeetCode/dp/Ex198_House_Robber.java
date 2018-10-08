package 数据结构.LeetCode.dp;

/**

 You are a professional robber planning to rob houses along a street. Each house has a
 certain amount of money stashed, the only constraint stopping you from robbing each of
 them is that adjacent houses have security system connected and it will automatically
 contact the police if two adjacent houses were broken into on the same night.

 Given a list of non-negative integers representing the amount of money of each house,
 determine the maximum amount of money you can rob tonight without alerting the police.

 */

public class Ex198_House_Robber {
    public int rob(int[] nums) {
        if(nums == null || nums.length == 0) return 0;

        int preYes = 0;  //之前的位置被抢劫的情况下，获得的最大金额
        int preNo = 0;   //之前的位置没被抢劫的情况下，获得的最大金额
        for(int i = 0;i<nums.length; i++){
            int curYes = preNo + nums[i];  //抢劫当前位置，则之前不能被抢劫
            int curNo = Math.max(preYes, preNo); //不抢劫当抢位置，则之前位置可以抢劫、也可以不抢劫，选择较大的
            preYes = curYes; //下一轮
            preNo = curNo;
        }
        return Math.max(preYes, preNo);
    }
}
