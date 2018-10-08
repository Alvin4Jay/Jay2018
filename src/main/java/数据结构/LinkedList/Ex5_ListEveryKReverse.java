package 数据结构.LinkedList;

import java.util.Stack;

/**
 *  链表中每K个逆序，最后一组若不满K个节点，则不逆序
 利用一个栈进行反转排序，从头开始遍历元素，每遍历一个元素，将其取下（next置为null），放入到栈中，
 当栈中元素达到k个时，对栈中的元素进行出栈，记录好头部和尾部结点，并链接到上一个反转后的尾部。
 由于利用了一个栈，因此这种算法    时间复杂度为O(N)，空间复杂度为O(K)。
 */
public class Ex5_ListEveryKReverse {
    public static ListNode reverse(ListNode head, int k){
        if(head == null || head.next == null || k < 2) return head;

        ListNode reverseHead = null;  //链表最终反转后的头结点
        ListNode next = null;  //原链表的临时节点

        ListNode header = null, tail = null; //一次反转过程中最终的头、尾节点
        ListNode lastTail = null;   //上一次反转过程中中的尾节点

        Stack<ListNode> stack = new Stack<ListNode>(); //存储中间k个节点

        while(head != null){
            next = head.next;
            head.next = null; //去掉引用
            stack.push(head); //入栈

            //判断栈中是否已经有k个元素
            if(stack.size() == k){
                //反转这k个元素
                header = tail = stack.pop();
                ListNode temp = null;
                while(!stack.isEmpty()){
                    temp = stack.pop();
                    tail.next = temp;
                    tail = temp;
                }
                //第一次反转
                if(reverseHead == null){
                    reverseHead = header;
                    lastTail = tail;
                }else { //非第一次反转
                    lastTail.next = header;
                    lastTail = tail;
                }
            }

            head = next; //继续遍历
        }

        //最后不满足k个节点或者总量不满足k个节点
        if(!stack.isEmpty()){
            header = stack.pop();  //头插法
            header.next = null; //最后一个节点
            ListNode temp = null;
            while (!stack.isEmpty()){
                temp = stack.pop();
                temp.next = header;
                header = temp;
            }
            //一次都未反转
            if(reverseHead == null){
                return header;
            }else {
                //接到lastTail上
                lastTail.next = header;
                return reverseHead;
            }
        }else {
            //刚好全部反转完成
            return reverseHead;
        }


    }

    public static void main(String[] args) {
        ListNode n1 = new ListNode(1);
        ListNode n2 = new ListNode(2);
        ListNode n3 = new ListNode(3);
        ListNode n4 = new ListNode(4);
        ListNode n5 = new ListNode(5);
        ListNode n6 = new ListNode(6);
        ListNode n7 = new ListNode(7);
        ListNode n8 = new ListNode(8);
        n1.next = n2;n2.next=n3;n3.next=n4;n4.next=n5;n5.next=n6;n6.next=n7;n7.next=n8;
        ListNode head = reverse(n1, 4);
//        ListNode head = reverse(n1, 3);
//        ListNode head = reverse(n1, 10);

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
