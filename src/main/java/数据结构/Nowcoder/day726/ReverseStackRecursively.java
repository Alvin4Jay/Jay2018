package 数据结构.Nowcoder.day726;

import java.util.Stack;

public class ReverseStackRecursively {

    /*
    链接：https://www.nowcoder.com/questionTerminal/ba7d7f5d1edf4d1690d66e12e951f6ea
    来源：牛客网

    一个栈依次压入1,2,3,4,5那么从栈顶到栈底分别为5,4,3,2,1。将这个栈转置后，从栈顶到栈底为
    1,2,3,4,5，也就是实现了栈中元素的逆序，请设计一个算法实现逆序栈的操作，但是只能用递归函
    数来实现，而不能用另外的数据结构。
    给定一个栈Stack以及栈的大小top，请返回逆序后的栈。
    测试样例：
    [1,2,3,4,5],5  左底右顶
    返回：[5,4,3,2,1]
     */
    //stack栈，top大小
    public static int[] reverseStackRecursively(int[] stack, int top) {
        if(stack == null || stack.length == 0 || top <= 0) return stack;
        int num = last(stack, top);
        int[] result = reverseStackRecursively(stack, --top);
        result[top] = num;
        return result;
    }

    /**
     * 拿到并移除栈底元素
     * @param stack 栈
     * @param top 栈大小
     * @return
     */
    public static int last(int[] stack, int top){
        int num = stack[--top];
        if(top == 0){
            return num;
        }else{
            int temp = last(stack, top); //期望获得的结果
            stack[--top] = num;
            return temp;
        }
    }

    /*
    一个栈依次压入1、2、3、4、5，那么从栈顶到栈底分别为5、4、3、2、1。将这个栈
    转置后，从栈顶到栈底为1、2、3、4、5，也就是实现栈中元素的逆序，但是只能用递
    归函数来实现，不能用其他数据结构。
     */
    //逆序一个栈
    public static void reverse(Stack<Integer> stack){
        if(stack == null || stack.isEmpty()) return ;
        int num = getLastElement(stack);
        reverse(stack);
        stack.push(num);
    }
    //获得并移除栈底元素
    public static int getLastElement(Stack<Integer> stack){
        int num = stack.pop();
        if(stack.isEmpty()){
            return num;
        }else {
            int temp = getLastElement(stack);
            stack.push(num);
            return temp;
        }
    }

    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        for (int i : stack) {
            System.out.print(i + " ");
        }
        System.out.println();
        ReverseStackRecursively.reverse(stack);
        for (int i : stack) {
            System.out.print(i + " ");
        }

        System.out.println("----------");
        int[] a = new int[]{1,2,3,4,5};
        a = ReverseStackRecursively.reverseStackRecursively(a,a.length);
        for (int i : a) {
            System.out.print(i + " ");
        }

    }
}
