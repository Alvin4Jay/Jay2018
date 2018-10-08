package 数据结构.LinkedList;

/**
 *  链表删除指定节点
 现在有一个单链表。链表中每个节点保存一个整数，再给定一个值val，把所有等于val的节点删掉。
 给定一个单链表的头结点head，同时给定一个值val，请返回清除后的链表的头结点，保证链表中有不等于该值的其它值。请保证其他元素的相对顺序。
 测试样例：
 {1,2,3,4,3,2,1},2
 {1,3,4,3,1}
 */
public class Ex6_ListDeleteSpecifiedElement {
    //创建一条新的链表
    public static ListNode deleteSpecifiedElement(ListNode head, int val){
        if(head == null) return null;

        ListNode first = null;  //新链表头结点
        ListNode last  = null;  //新链表尾节点
        ListNode current = head;  //当前节点
        ListNode temp = null;    //临时节点

        while(current != null){
            if(current.val != val){
                temp = new ListNode(current.val);
                if(first == null){
                    first = last = temp;
                }else{
                    last.next = temp;
                    last = temp;
                }
            }
            current = current.next;
        }
        return first;
    }

    public static void main(String[] args) {
        ListNode n1 = new ListNode(1);
        ListNode n2 = new ListNode(2);
        ListNode n3 = new ListNode(3);
        ListNode n4 = new ListNode(4);
        ListNode n5 = new ListNode(3);
        ListNode n6 = new ListNode(2);
        ListNode n7 = new ListNode(1);
        n1.next = n2;n2.next=n3;n3.next=n4;n4.next=n5;n5.next=n6;n6.next=n7;
        ListNode head = deleteSpecifiedElement(n1, 2);

        while(head != null){
            System.out.print(head.val +  " ");
            head = head.next;
        }
    }

    static class ListNode{
        int val;
        ListNode next = null;
        ListNode(int val) {
            this.val = val;
        }
    }
}
