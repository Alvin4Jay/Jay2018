package 数据结构.StackAndQueue;

import java.util.Stack;

/**
 * 双栈实现队列
 */
public class Ex2_QueueByTwoStack {
    private Stack<Integer> data = new Stack<Integer>();
    private Stack<Integer> temp = new Stack<Integer>();

    public void enqueue(int item){
        data.push(item);
    }

    public int dequeue(){
        if(data.isEmpty()){
            throw new RuntimeException("queue is empty");
        }

        while(!data.isEmpty()){
            temp.push(data.pop());
        }
        int res = temp.pop();
        while(!temp.isEmpty()){
            data.push(temp.pop());
        }
        return res;
    }

    public static void main(String[] args) {
        Ex2_QueueByTwoStack queue = new Ex2_QueueByTwoStack();
        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);
        queue.enqueue(4);
        queue.enqueue(5);
        queue.enqueue(6);
        queue.enqueue(7);
        queue.enqueue(8);
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());

    }

}
