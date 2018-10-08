package 数据结构.LeetCode.greedy;

/**
 Say you have an array for which the ith element is the price of a given stock on day i.

 Design an algorithm to find the maximum profit. You may complete as many transactions as
 you like (ie, buy one and sell one share of the stock multiple times). However, you may not
 engage in multiple transactions at the same time (ie, you must sell the stock before you buy again).
 */
public class Ex122_Best_Time_to_Buy_and_Sell_Stock_II {
    public int maxProfit(int[] prices) {
        if(prices == null || prices.length <= 1) return 0;
        int sum = 0;
        for(int i=1;i<prices.length; i++){
            if(prices[i] > prices[i-1]){
                sum += (prices[i]-prices[i-1]); //这道题股票 支持一天内，符合条件的情况下先抛出再买入。
            }
        }
        return sum;
    }
}
