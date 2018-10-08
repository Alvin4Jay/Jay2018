package 数据结构.Sword2Offer;

import java.util.Stack;

/**
 * 栈的压入、弹出序列
 */
public class P22_StackPushPopOrder {
    public boolean isPopOrder(int [] pushA,int [] popA) {
        if(pushA == null || popA == null) return false;
        if(pushA.length == 0 || popA.length == 0) return false;
        if(pushA.length == 1 && popA.length == 1) return pushA[0] == popA[0];
        if(pushA.length == 2 && popA.length == 2){
            if(pushA[0] == popA[0] && pushA[1]==popA[1]){
                return true;
            }else if(pushA[0] == popA[1] && pushA[1]==popA[0]){
                return true;
            }else {
                return false;
            }
        }

        //popA顺序中出现c a b的情况 c>b and b>a
        for (int i = 0; i < popA.length -2; i++) {
            if(popA[i] > popA[i+2] && popA[i+2] > popA[i+1]){
                return false;
            }
        }
        return true;
    }

    public boolean isPopOrder2(int[] pushA, int[] popA){
        if(pushA == null || popA == null) return false;
        if(pushA.length == 0 || popA.length == 0) return false;

        Stack<Integer> s = new Stack<>(); //辅助栈
        int popIndex = 0;
        for (int i = 0; i < pushA.length; i++) { //i遍历pushA, j表示popA的位置
            s.push(pushA[i]);

            while(!s.isEmpty() && s.peek() == popA[popIndex]){
                s.pop();
                popIndex ++;
            }
        }
        return s.isEmpty(); //如果符合要求，最后辅助栈中将无元素
    }
}
