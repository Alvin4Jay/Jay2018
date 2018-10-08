package 数据结构.LinkedList;

/**
 *  两条单链表是否相交
 *
 1. 思路一：
 利用哈希表，首先遍历一遍链表A，将A的所有结点放入到哈希表中，再遍历一遍B，遍历时，
 判断哈希表中是否有相同的结点，有表示相交；
 此时空间复杂度为O(N)，不满足要求

 2. 思路二：
 如果两个链表相交，后端对齐后，那说明两个链表的最后一个结点相同。因此，这种思路是，
 分别遍历链表A和B，指针指向两个链表的最后一
 个结点，如果租后一个结点相同，则表示相交。这种方式，虽然空间复杂度为O(1)，但是这
 种方式不能够知道最初相交的结点在哪里

 3. 思路三：
 如果要知道最初相交的结点在哪里，可用如下方法。首先分别遍历一遍链表A和B，得到A和B
 的长度m和n，
 如果m>n，A从头开始先遍历m-n步，如果m<n，B先遍历n-m步，此后A、B同时移动，如果遍历
 过程中A和B的当前结点相同，
 表示两个链表相交，且当前结点为相交的第一个结点。
 */
public class Ex10_TwoListInter {
    /**
     * 2.如果两个链表相交，后端对齐后，那说明两个链表的最后一个结点相同。因此，这种
     * 思路是，分别遍历链表A和B，
     * 指针指向两个链表的最后一个结点，如果租后一个结点相同，则表示相交。这种方式，
     * 虽然空间复杂度为O(1)，
     * 但是这种方式不能够知道最初相交的结点在哪里
     * @param n1
     * @param n2
     * @return
     */
    public boolean checkIntersect2(Node n1, Node n2){
        Node last1 = n1;
        Node last2 = n2;

        while(last1.next != null){
            last1 = last1.next;
        }
        while(last2.next != null){
            last2 = last2.next;
        }

        return last1 == last2; //判断最后一个节点是否相等
    }


    //思路三 时间复杂度O(N) 空间复杂度 O(1)
    public boolean checkIntersect3(Node headA, Node headB){
        if(headA == null || headB == null) return false;

        int m = 0;
        int n = 0;
        int dst = 0; //长度差

        Node tempA = headA;
        Node tempB = headB;
        //得到两条链表的长度m,n
        while(tempA != null){
            m++;
            tempA = tempA.next;
        }
        while(tempB != null){
            n ++;
            tempB = tempB.next;
        }

        tempA = headA;
        tempB = headB;
        if(m >= n){ //长的链表先移动dst
            dst = m - n;
            for (int i = 0; i < dst; i++) {
                tempA = tempA.next;
            }
        }else {
            dst = n - m;
            for (int i = 0; i < dst; i++) {
                tempB = tempB.next;
            }
        }

        while(tempA != null && tempB != null){
            if(tempA == tempB){
                return true;
            }
            tempA = tempA.next;
            tempB = tempB.next;
        }
        return false;
    }

    private static class Node{
        int value;
        Node next;
        Node(int value){
            this.value = value; //位置
        }
    }

}
