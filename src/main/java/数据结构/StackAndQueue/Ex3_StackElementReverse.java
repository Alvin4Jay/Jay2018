package 数据结构.StackAndQueue;

import java.util.Stack;

/**
 *  反转栈中元素，（重要）不能使用其他数据结构
 *
 *  http://www.cnblogs.com/4everlove/p/3666016.html
 *
 *  1 2 3 4 5（左底右顶）---> 5 4 3 2 1
 */
public class Ex3_StackElementReverse {

    public static void reverseStack(Stack<Integer> stack){
        //递归返回条件
        if(stack.isEmpty()){
            return;  //栈内没有元素,直接返回
        }
        int temp = stack.pop();
        if(stack.isEmpty()){ //栈中只有一个元素，直接返回
            stack.push(temp);
            return;
        }else {
            stack.push(temp); //栈中元素>=2, 反转
        }

        //具体翻转过程，归纳证明 1 2 3 4 5
        int temp1 = stack.pop(); //取出peek元素  5
        reverseStack(stack);  //反转stack 4 3 2 1
        int temp2 = stack.pop(); //取出头部元素 1
        reverseStack(stack); //2 3 4
        stack.push(temp1); //2 3 4 5

        reverseStack(stack); //  5 4 3 2 (缩小范围反转栈)
        stack.push(temp2);  // 5 4 3 2 1
    }

    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        stack.push(4);
        stack.push(5);
        for (Integer integer : stack) {
            System.out.print(integer + " ");
        }
        System.out.println("\r\n************");

        reverseStack(stack);

        for (Integer integer : stack) {
            System.out.print(integer + " ");
        }

    }

}
