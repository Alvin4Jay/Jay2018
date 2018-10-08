package 数据结构.Sword2Offer;

/**
 *  找到链表倒数第k个节点（从1开始记，双指针）
 */
public class P15_FindKthToTail {

    //双指针只需遍历一边
    public ListNode FindKthToTail_Pro(ListNode head,int k) {
        if(head == null || k <= 0){
            return null;
        }

        ListNode ahead = head; //先走k-1步
        ListNode behind = head; //后面一起走

        for (int i = 0; i < k - 1; i++) {
            if(ahead.next != null){ //（重要）
                ahead = ahead.next; //走k-1步
            }else {
                return null;  //该检查防止节点总数小于k
            }
        }

        while(ahead.next != null){ //一起走到链表尾部
            ahead = ahead.next;
            behind = behind.next;
        }
        return behind;
    }

    //遍历两遍
    public ListNode FindKthToTail(ListNode head,int k) {
        if(head == null || k <=0)  return null;
        int size = 0;
        ListNode current = head;
        while(current != null){  //第一遍遍历，获取总大小
            size++;
            current = current.next;
        }
        if(k>size) return null; //判断k是否大于size


        int index = 0;
        current = head;
        while(index != size- k){ //第二遍遍历，得到倒数第k个节点
            index ++;
            current = current.next;
        }

        return current;

    }


    static class ListNode {
        int val;
        ListNode next = null;

        ListNode(int val) {
            this.val = val;
        }
    }
}
