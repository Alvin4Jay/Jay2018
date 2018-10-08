package 数据结构.Sword2Offer;

import java.util.Stack;

/**
 * 从尾到头打印链表
 */
public class P5_PrintListFromTailToHead {
    public static void main(String[] args) {
        ListNode node1 = new ListNode();
        ListNode node2 = new ListNode();
        ListNode node3 = new ListNode();
        node1.key = 1;
        node2.key = 2;
        node3.key = 3;

        node1.next = node2;
        node2.next = node3;

        //printListReversing(node1);
        printListReversingByRecursion(node1);
    }

    /**
     * 利用栈实现从尾到头打印链表
     * @param node 头结点
     */
    public static void printListReversing(ListNode node){
        Stack<ListNode> stack = new Stack<>();

        while(node != null){
            stack.push(node);
            node = node.next;
        }

        while(!stack.isEmpty()){
            ListNode listNode = stack.pop();
            System.out.printf("%d\t",listNode.key);
        }
    }

    /**
     * 递归遍历
     * @param node 头结点
     */
    public static void printListReversingByRecursion(ListNode node){
        if(node != null){
            if(node.next != null){
                printListReversingByRecursion(node.next);
            }
            System.out.printf("%d\t",node.key);
        }
    }

    static class ListNode{
        int key;
        ListNode next;
    }
}

