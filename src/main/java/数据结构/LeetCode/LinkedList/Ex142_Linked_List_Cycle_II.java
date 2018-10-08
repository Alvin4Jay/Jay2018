package 数据结构.LeetCode.LinkedList;

/**
 Given a linked list, return the node where the cycle begins. If there is no cycle, return null.

 Note: Do not modify the linked list.

 Follow up:
 Can you solve it without using extra space?

 双指针
 */
public class Ex142_Linked_List_Cycle_II {
    public ListNode detectCycle(ListNode head) {
        if(head == null || head.next == null ) return null;

        ListNode slow =  head; //慢指针
        ListNode fast = head;  //快指针

        while(fast != null && fast.next != null){ //快慢指针能走两步
            slow = slow.next;
            fast = fast.next.next;
            if(slow == fast) break; //快慢指针相遇，说明存在环
        }
        //无环
        if(fast == null || fast.next == null) return null; //无环的情况

        //有环
        fast = head; //fast重新赋值为head

        while(fast != slow) { //同一速度走
            fast = fast.next;
            slow = slow.next;
        }
        return fast; //相遇返回
    }

    private static class ListNode {
        int val;
        ListNode next;
        ListNode(int x) {
            val = x;
            next = null;
        }
    }
}
