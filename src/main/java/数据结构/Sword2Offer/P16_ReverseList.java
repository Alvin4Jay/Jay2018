package 数据结构.Sword2Offer;

import java.util.ArrayList;
import java.util.List;

/**
 * 反转链表(双指针)
 */
public class P16_ReverseList {
    //双指针解法,时间复杂度O(n)  空间复杂度O(1)
    public ListNode ReverseList_Pro(ListNode head) {
        if (head == null) return null;

        ListNode pre = null; //head前一个节点
        ListNode next = null; //head后一个节点

        while (head != null) { //退出条件是head走到链表尾部
            next = head.next; //next指向head的后一个节点
            head.next = pre; //断开head与next之间的连接，指向pre
            pre = head; //pre移动到head
            head = next; //head移动到next
        }
        return pre;
    }

    //用栈应该也可以实现

    //普通解法O(n) O(n)
    public ListNode ReverseList(ListNode head) {
        if(head == null) return null;

        ListNode c = head;
        List<Integer> list = new ArrayList<Integer>();
        while(c != null){
            list.add(c.val);
            c = c.next;
        }

        ListNode h = null; //重新构造链表返回
        ListNode last = null;
        int size = list.size();
        for(int i=0;i< size;i++){

            if(h==null && last ==null){ //第一次插入
                h = new ListNode(list.get(size-i-1));
                last  = h;
            }else{
                ListNode temp = new ListNode(list.get(size-i-1)); //尾部插入
                last.next = temp;
                last = temp;
            }
        }
        return h; //返回头结点
    }

    static class ListNode {
        int val;
        ListNode next = null;

        ListNode(int val) {
            this.val = val;
        }
    }
}
