package 数据结构.LinkedList;

import java.util.Stack;

/**
 * Created by Jay on 2017/10/12
 */
public class LinkedListPalindrome {

    public static boolean isPalindrome(Node head){
        Stack<Node> stack = new Stack<>();
        Node cur = head;
        while(cur != null){
            stack.push(cur);
            cur = cur.next;
        }

        while(head != null){
            if(head.value != stack.pop().value){
                return false;
            }
        }
        return true;
    }

    static class Node{
        public int value;
        public Node next;

        public Node(int value) {
            this.value = value;
        }
    }
}
