package 数据结构.LinkedList;

/**
  环形链表插值有一个整数val，如何在节点值有序的环形链表中插入一个节点值为val的节点，并且保证这个环形单链表依然有序。
  给定链表的信息，及元素的值A及对应的nxt指向的元素编号同时给定val，请构造出这个环形链表，并插入该值。
  测试样例：
 [1,3,4,5,7], [1,2,3,4,0], 2
 返回：{1,2,3,4,5,7}

 */
public class Ex1_CircleListInsertValue {
    /**
     *  插入链表节点
     * @param A 原链表的值
     * @param nxt A数组中每个节点指向下一个节点的节点编号（从0开始）
     * @param val 要插入的值
     * @return 头结点
     */
    public static ListNode insert(int[] A, int[] nxt, int val){
        ListNode node = new ListNode(val);

        //情况一 原链表为空
        if(A == null || A.length == 0) {
            node.next = node; //成环
            return node;
        }

        //建立环形链表，建立一条单链表，可以不成环
        ListNode head = new ListNode(A[0]); //头结点
        ListNode temp = head;
        for (int i = 0; i < A.length-1; i++) {
            ListNode newNode = new ListNode(A[nxt[i]]);
            temp.next = newNode;
            temp = newNode; //最后指向最后一个节点
        }

        //情况二 插入值小于头节点值，变更head
        if(val < head.val){
            node.next = head;
            head = node;
            temp.next = head; //成环
            return head;
        }

        //情况三 插入值大于最后一个值，不变更head
       if(val > temp.val){
            temp.next = node;
            node.next = head;
            return head;
        }

        //情况四 节点之间插入
        ListNode p = head;
        ListNode n = head.next;
        while(n != null){
            if(p.val <= val && val <= n.val){
                break;
            }
            p = n;
            n = n.next;
        }
        if(p.val < val){
            p.next = node;
            node.next = head;
            return head;
        }
        node.next = n; //节点中间插入
        p.next = node;
        return head;
    }

    public static void main(String[] args) {
        int[] a = {1,3,4,5,7};
        int[] b = {1,2,3,4,0};
        ListNode node = insert(a, b ,3);
        System.out.println(node.val);
    }

    static class ListNode{
        int val;
        ListNode next = null;
        ListNode(int val){
            this.val = val;
        }
    }
}

