package 数据结构.LeetCode.Array;

/**
 Suppose you have a long flowerbed in which some of the plots are planted and some are not.
 However, flowers cannot be planted in adjacent plots - they would compete for water and both would die.

 Given a flowerbed (represented as an array containing 0 and 1, where 0 means empty and 1 means not empty),
 and a number n, return if n new flowers can be planted in it without violating the no-adjacent-flowers rule.

 Example 1:
 Input: flowerbed = [1,0,0,0,1], n = 1
 Output: True
 Example 2:
 Input: flowerbed = [1,0,0,0,1], n = 2
 Output: False

 Note:
 1.The input array won't violate no-adjacent-flowers rule.
 2.The input array size is in the range of [1, 20000].
 3.n is a non-negative integer which won't exceed the input array size.

 */
public class Ex605_Can_Place_Flowers {
    public boolean canPlaceFlowers(int[] flowerbed, int n) {
        int count = 0; //已放置多少花
        for(int i=0; (i<flowerbed.length) && (count<n); i++){  //小于总数，count要插入的花总数未到
            if(flowerbed[i] == 0){  //元素为1，不能种花，跳过；元素为0，接下来判断
                int prev = i==0 ? 0 : flowerbed[i-1]; //前一个元素的值，如果i是0，prev为0
                int next = i==flowerbed.length-1? 0 : flowerbed[i+1]; //后一个元素的值，如果i到达右边界，next=0
                if(prev==0 && next==0){
                    flowerbed[i] = 1;
                    count ++;
                }
            }
        }
        return count == n;
    }
}
