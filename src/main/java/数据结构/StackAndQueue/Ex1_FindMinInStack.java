package 数据结构.StackAndQueue;

import java.util.Stack;

/**
 *  可查询最值min的栈（双栈实现）
 */
public class Ex1_FindMinInStack {
    private Stack<Integer> data = new Stack<Integer>(); //保存数据
    private Stack<Integer> min = new Stack<Integer>();  //保存最小值

    public void push(int item){
        data.push(item);

        if(min.isEmpty()){ //当前存储最小值的栈为空
            min.push(item);
        }else {
            if(item < min.peek()){  //当前入栈元素item比min栈中元素小，入栈
                min.push(item);
            }else {  //item不入栈
                min.push(min.peek());
            }
        }
    }

    public int pop(){
        if(data.isEmpty()){
            throw new RuntimeException("stack is empty");
        }
        min.pop();
        return data.pop();
    }

    /**
     * 返回最小元素
     * @return
     */
    public int min(){
        return min.peek();
    }

    public static void main(String[] args) {
        Ex1_FindMinInStack stack = new Ex1_FindMinInStack();
        stack.push(3);
        System.out.println(stack.min());  //3
        stack.push(2);
        System.out.println(stack.min());  //2
        stack.push(1);
        System.out.println(stack.min());  //1
        stack.push(2);
        System.out.println(stack.min());  //1
        stack.pop();
        System.out.println(stack.min());  //1
        stack.pop();
        System.out.println(stack.min());  //2
        stack.pop();
        System.out.println(stack.min());  //3
    }

}
