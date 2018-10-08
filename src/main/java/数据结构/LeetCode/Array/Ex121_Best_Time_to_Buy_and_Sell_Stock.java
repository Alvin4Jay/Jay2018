package 数据结构.LeetCode.Array;

/**
 Say you have an array for which the ith element is the price of a given stock on day i.

 If you were only permitted to complete at most one transaction (ie, buy one and sell one
 share of the stock), design an algorithm to find the maximum profit.

 Example 1:
 Input: [7, 1, 5, 3, 6, 4]
 Output: 5

 max. difference = 6-1 = 5 (not 7-1 = 6, as selling price needs to be larger than buying price)
 Example 2:
 Input: [7, 6, 4, 3, 1]
 Output: 0

 In this case, no transaction is done, i.e. max profit = 0.
 */
public class Ex121_Best_Time_to_Buy_and_Sell_Stock {
    public int maxProfit(int[] prices) {
        /*int max = 0;
        for (int i = 0; i < prices.length; i++) {
            for (int j = i+1; j < prices.length; j++) {
                if(prices[i] < prices[j]){
                    int s = prices[j]-prices[i];
                    max = Math.max(s, max);
                }
            }
        }
        return max;*/
        //[7, 1, 5, 3, 6, 4]
        if(prices == null || prices.length <= 1) return 0;

        int maxProfit = 0; //目前的最大收益
        int soFarMin = prices[0]; //目前的最小数
        for(int i= 1; i< prices.length ;i++){
            if(prices[i] > soFarMin){
                maxProfit = Math.max(maxProfit, prices[i] - soFarMin);
            }else{
                soFarMin = prices[i];
            }
        }
        return maxProfit;
    }

    public static void main(String[] args) {
        System.out.println(new Ex121_Best_Time_to_Buy_and_Sell_Stock()
                .maxProfit(new int[]{7, 1, 5, 3, 6, 4}));
        System.out.println(new Ex121_Best_Time_to_Buy_and_Sell_Stock()
                .maxProfit(new int[]{7, 6, 4, 3, 1}));
    }
}
