package 数据结构.StackAndQueue;

import java.util.Stack;

/**
 *  栈中元素按升序排序
    按升序对栈进行排序（即最大元素位于栈顶），要求最多只能使用一个
    额外的栈存放临时数据，但不得将元素复制到别的数据结构中。

    stack栈取出元素value  -----  help栈中元素可以分为小于value的靠近栈底的元素部分， 和   大于value的靠近栈顶的元素部分
    然后将大于的部分压入stack栈，之后value压入help栈，再把之前压入stack栈的元素部分压入help栈。

 */
public class Ex4_StackElementSort {

    public static Stack<Integer> stackSort(Stack<Integer> stack){
        Stack<Integer> help = new Stack<Integer>();  //辅助栈

        while(!stack.isEmpty()){
            int value = stack.pop(); //得到stack栈顶元素value

            while(!help.isEmpty() && help.peek() > value){ //将help栈中大于value元素的元素压入stack栈
                stack.push(help.pop());
            }
            help.push(value); //将value压入help栈， 压入之前help栈中元素均小于value
        }
        return help;
    }


    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(2);
        stack.push(5);
        stack.push(3);
        stack.push(1);
        stack.push(4);

        Stack<Integer> res = stackSort(stack);

        for (Integer re : res) {
            System.out.print(re + " ");
        }

    }

}
