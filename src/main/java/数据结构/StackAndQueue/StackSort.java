package 数据结构.StackAndQueue;

import java.util.Stack;

/**
 * 站排序
 */
public class StackSort {
    public static void stackSort(Stack<Integer> stack){
        Stack<Integer> help = new Stack<>();
        while(!stack.isEmpty()){
            int v = stack.pop();
            while(!help.isEmpty() && v > help.peek()){
                stack.push(help.pop());
            }
            help.push(v);
        }
        while(!help.isEmpty()){
            stack.push(help.pop());
        }
    }

    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
        stack.push(3);
        stack.push(1);
        stack.push(2);
        stack.push(6);
        stack.push(4);
        stack.push(8);
        stackSort(stack);
        for (int integer : stack) {
            System.out.print(integer + " ");
        }
    }

}
