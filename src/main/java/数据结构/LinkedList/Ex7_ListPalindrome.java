package 数据结构.LinkedList;

import java.util.Stack;

/**
 *  判断链表是否是回文结构
 *  利用栈 时间复杂度 O(N) 空间复杂度 O(N)
 */
public class Ex7_ListPalindrome {

    public static boolean isPalindrome(ListNode head){
        if(head == null) return false;

        Stack<ListNode> stack = new Stack<ListNode>();
        ListNode first = head;
        //元素全部入栈
        while(first != null){
            stack.push(first);
            first = first.next;
        }

        //检查
        ListNode current = head;
        while(!stack.isEmpty() && current != null){
            ListNode temp = stack.pop();
            if(current.val != temp.val){
                return false;
            }else {
                current = current.next;
            }
        }
        return true;
    }

    /*
    //利用快慢指针和栈
    public boolean isPalindrome(ListNode pHead) {
        if(pHead == null){
            return false;
        }
        StackIteration<ListNode> stack = new StackIteration<ListNode>();
        ListNode fast = pHead;
        ListNode slow = pHead;
        while(fast != null){
            if(fast.next == null){//表示到快指针达尾部，说明链表结点个数为奇数，此时慢指针到达中部，且此时慢指针指向的结点不需要入栈
                slow = slow.next;//链表结点数为奇数时，跳过中间结点
                break;
            }
            stack.push(slow);
            slow = slow.next;
            fast = fast.next.next;
        }
        ListNode temp;
        while(slow != null && !stack.isEmpty()){//后面慢指针和栈顶结点值依次比较
            temp = stack.pop();
            if(temp.val != slow.val){
                return false;
            }else{
                slow = slow.next;
            }
        }
        return true;
    }
    */

    public static void main(String[] args) {
        ListNode n1 = new ListNode(1);
        ListNode n2 = new ListNode(2);
        ListNode n3 = new ListNode(3);
        ListNode n4 = new ListNode(2);
        ListNode n5 = new ListNode(1);
        n1.next = n2;n2.next=n3;n3.next=n4;n4.next=n5;

        System.out.println(isPalindrome(n1));

    }

    static class ListNode{
        int val;
        ListNode next = null;
        ListNode(int val) {
            this.val = val;
        }
    }
}
