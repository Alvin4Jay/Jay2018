package 数据结构.LinkedList;


import java.util.Stack;

/**
 * 链表共同的部分
 */
public class Ex11_CommonPart {

    public static void printCommmonPart(Node head1, Node head2){
        System.out.print("Common Part: ");
        while(head1 != null && head2 != null){
            if(head1.value < head2.value){
                head1 = head1.next;
            }else if(head1.value > head2.value){
                head2 = head2.next;
            }else {
                System.out.print(head1.value + " ");
                head1 = head1.next;
                head2 = head2.next;
            }
        }
        System.out.println();
    }

    static class Node{
        public int value;
        public Node next;

        public Node(int value) {
            this.value = value;
        }
    }
    //两链表各位相加
    public static Node addList(Node head1, Node head2){
        Stack<Integer> s1 = new Stack<>();
        Stack<Integer> s2 = new Stack<>();
        while(head1 != null){
            s1.push(head1.value);
            head1 = head1.next;
        }
        while(head2 != null){
            s1.push(head2.value);
            head2 = head2.next;
        }

        int t = 0;
        int n1 = 0;
        int n2 = 0;
        int n = 0;
        Node pre = null;
        Node cur = null;
        while(!s1.isEmpty() || !s2.isEmpty()){
            n1 = s1.isEmpty()? 0 : s1.pop();
            n2 = s2.isEmpty()? 0 : s2.pop();
            n = n1 + n2 + t;
            pre = cur;
            cur = new Node(n%10);
            cur.next = pre;
            t = t/10;
        }
        if(t == 1){
            pre = cur;
            cur = new Node(1);
            cur.next = pre;
        }
        return cur;
    }

}
