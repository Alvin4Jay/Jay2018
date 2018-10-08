package 数据结构.Sword2Offer;

/**
 * 合并两个递增排序的链表（递归）
 */
public class P17_ListMerge {
    /**
     * Merge两个递增排序的链表（递归，双指针）
     * @param list1 链表1
     * @param list2 链表2
     * @return 拼接起来的链表
     */
    public ListNode Merge(ListNode list1,ListNode list2) {
        //递归返回条件
        if(list1 == null && list2 == null) return null;
        if(list1 == null) return list2; //list1为空
        if(list2 == null) return list1; //list2为空

        ListNode head = null;

        if(list1.val <= list2.val){
            head = new ListNode(list1.val);
            head.next = Merge(list1.next, list2);
        }else {
            head = new ListNode(list2.val);
            head.next = Merge(list1, list2.next);
        }
        return head;
    }

    static class ListNode {
        int val;
        ListNode next = null;

        ListNode(int val) {
            this.val = val;
        }
    }
}


