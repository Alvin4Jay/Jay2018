package 数据结构.LinkedList;

/**
 *  链表的分化

 对于一个链表，我们需要用一个特定阈值完成对它的分化，使得小于等于这个值的结点移到前面，大于该值的结点在后面，同时保证两类结点内部的位置关系不变。
 给定一个链表的头结点head，同时给定阈值val，请返回一个链表，使小于等于它的结点在前，大于等于它的在后，保证结点值不重复。
 测试样例：
 {1,4,2,5},3
 {1,2,4,5}
 思路：
 定义一个指向结点值<=val的结点的链表，一个结点值>val的结点的链表，开始从头结点开始遍历，遍历到当前结点的时候，
 先用next指向当前结点的后面链表，取出当前结点，对当前结点的值进行比较，决定放到哪个链表中。这里要特别注意，
 对于这种链表分化的操作，我们在遍历链表的时候，一定要把当前结点取出来，将其next置为null，即脱离原来的链表，
 否则，如果不把当前结点取出next置null，在组拼两个链表后，组拼后的链表中可能存在环！

 */
public class Ex3_ListDivide {

    public static ListNode listDivide(ListNode head, int val){
        //头结点为空或者只有一个节点
        if(head == null || head.next == null){
            return head;
        }

        ListNode list1 = null; //小于等于val
        ListNode list2 = null; //大于等于val
        ListNode temp1 = null; //list1尾指针
        ListNode temp2 = null; //list2尾指针

        ListNode current = head;
        ListNode next=  current.next;
        while(current != null){
            current.next = null; //防止list1、list拼接后有环存在
            if(current.val <= val){
                if(list1 == null){ //第一次插入
                    list1 = current;
                    temp1 = list1;
                }else {
                    temp1.next = current;
                    temp1 = temp1.next;
                }
            }else {
                if(list2 == null){ //第一次插入
                    list2 = current;
                    temp2 = list2;
                }else {
                    temp2.next = current;
                    temp2 = temp2.next;
                }
            }
            if(next == null){
                break;
            }
            current = next;
            next = current.next;
        }

        temp1.next = list2;
        return list1;
    }

    public static void main(String[] args) {
        // 1 4 7 2 5相对位置不变
        ListNode n1 = new ListNode(1);
        ListNode n2 = new ListNode(4);
        ListNode n3 = new ListNode(7);
        ListNode n4 = new ListNode(2);
        ListNode n5 = new ListNode(5);

        n1.next = n2;
        n2.next = n3;
        n3.next = n4;
        n4.next = n5;

        ListNode node = listDivide(n1, 3);

        while(node != null){
            System.out.print(node.val + " ");
            node = node.next;
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
