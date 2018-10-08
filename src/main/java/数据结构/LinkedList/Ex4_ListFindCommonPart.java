package 数据结构.LinkedList;

import java.util.ArrayList;
import java.util.List;

/**
 *  两个升序链表找到公共部分
 *
 现有两个升序链表，且链表中均无重复元素。请设计一个  高效  的算法，打印两个链表的公共值部分。
 给定两个链表的头指针headA和headB，请返回一个vector，元素为两个链表的公共部分。请保证返回数组的升序。
 两个链表的元素个数均小于等于500。保证一定有公共值
 测试样例：
 {1,2,3,4,5,6,7},{2,4,6,8,10}
 返回：[2.4.6] 数组形式返回

思路：
 由于链表都是有序的，因此从头结点开始遍历两个链表，如果headA指向的结点值小于headB指向的结点值，
 headA往后移动一次，如果headA指向的结点值大于headB指向的结点值，headB往后移动一次，如果相等，
 则可以打印该值，两个链表同时移动。
 */
public class Ex4_ListFindCommonPart {

    public static int[] findCommonPart(ListNode head1, ListNode head2){

        if(head1 == null || head2 == null) return null;

        List<Integer> list = new ArrayList<Integer>(); //存放共同的值
        ListNode temp1 = head1;
        ListNode temp2 = head2;

        while(temp1 != null && temp2 != null){
            if(temp1.val < temp2.val){
                temp1 = temp1.next;
            }else if(temp1.val > temp2.val){
                temp2 = temp2.next;
            }else {
                list.add(temp1.val);
                temp1 = temp1.next;
                temp2 = temp2.next;
            }
        }

        int[] arr = new int[list.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    public static void main(String[] args) {
        //{1,2,3,4,5,6,7},{2,4,6,8,10} 2 4 6
        ListNode n1 = new ListNode(1);
        ListNode n2 = new ListNode(2);
        ListNode n3 = new ListNode(3);
        ListNode n4 = new ListNode(4);
        ListNode n5 = new ListNode(5);
        ListNode n6 = new ListNode(6);
        ListNode n7 = new ListNode(7);
        n1.next=n2;n2.next=n3;n3.next=n4;n4.next=n5;n5.next=n6;n6.next=n7;

        ListNode n8 = new ListNode(2);
        ListNode n9 = new ListNode(4);
        ListNode n10 = new ListNode(6);
        ListNode n11 = new ListNode(8);
        ListNode n12 = new ListNode(10);
        n8.next=n9;n9.next=n10;n10.next=n11;n11.next=n12;

        int[] arr = findCommonPart(n1, n8);
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
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
