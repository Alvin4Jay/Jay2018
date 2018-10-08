package 数据结构.LeetCode.LinkedList;

/**
 Given a linked list, determine if it has a cycle in it.

 Follow up:
 Can you solve it without using extra space?

O(n)时间复杂度 O(1)空间复杂度
 双指针解法
 */
public class Ex141_Linked_List_Cycle {
    public boolean hasCycle(ListNode head) {
        if(head == null || head.next == null ) return false; //0或1，返回false

        ListNode slow = head; //慢指针
        ListNode fast = head; //快指针

        while(fast != null && fast.next != null){
            slow = slow.next;
            fast = fast.next.next;
            if(slow == fast ){ //相遇说明有环
                return true;
            }

        }
        return false; //退出条件，说明不存在环
    }
    public static class ListNode {
        int val;
        ListNode next;
        ListNode(int x) {
            val = x;
            next = null;
        }
    }
}

