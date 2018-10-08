package 数据结构.Sword2Offer;

import java.util.Stack;

/**
 * 包含min函数的栈
 */
public class P21_StackWithMin {
    Stack<Integer> data = new Stack<Integer>();  //存放数据
    Stack<Integer> min = new Stack<Integer>();   //存放最小值

    public void push(int node) {
        data.push(node);
        if(!min.isEmpty()){
            int temp = min.peek();
            if(temp > node){
                min.push(node);
            }else {
                min.push(temp);
            }
        }else {
            min.push(node);
        }

        /*data.push(node);
        if(min.size() == 0 || node < min.peek()){
            min.push(node);
        }else {
            min.push(min.peek());
        }*/
    }

    public void pop() {
        if(!data.isEmpty()){
            data.pop();
            min.pop();
            return;
        }
        throw new RuntimeException("no elements");
    }

    public int top() {
        if(!data.isEmpty()){
            return data.peek();
        }
        throw new RuntimeException("no elements");
    }

    public int min() {
        if(!min.isEmpty()){
            return min.peek();
        }
        throw new RuntimeException("no elements");
    }
}
