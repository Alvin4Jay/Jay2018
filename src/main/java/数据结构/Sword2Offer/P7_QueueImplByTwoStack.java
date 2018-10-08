package 数据结构.Sword2Offer;

import java.util.Stack;

/**
 * 双栈实现队列
 */
public class P7_QueueImplByTwoStack {
    private Stack<Integer> stack1 = new Stack<Integer>(); //入队列使用
    private Stack<Integer> stack2 = new Stack<Integer>(); //出队列使用

    public void push(int node) {
        stack1.push(node);
    }

    public int pop() {
        while(!stack1.empty()){
            stack2.push(stack1.pop());
        }
        int temp = stack2.pop();
        while(!stack2.empty()){
            stack1.push(stack2.pop());
        }
        return temp;
    }

}
