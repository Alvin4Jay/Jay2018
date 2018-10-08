package 数据结构.NowcoderZuo.day720;

public class JosephusProblem {

    /**
     * O(MxN) 时间复杂度
     * @param head 头结点
     * @param m 需要报的数，从1开始
     * @return
     */
    public static Node josephus1(Node head, int m){
        if(head == null || head.next == head || m < 1) return head;

        Node last = head;
        while(last.next != head){
            last = last.next;
        }

        int count = 0; //报数
        while(last != head){ //不是只剩一个
            count ++;
            if(count != m){
                last = last.next; //没报到，l h指针前移
                head = last.next;
            }else{ //报到m
                last.next = head.next; //删除
                head = last.next; //调整
                count = 0; //清零
            }
        }
        return last;
    }

    public static Node josephus2(Node head, int m){
        if(head == null || head.next == head || m < 1) return head;

        int sum = 1;
        Node current = head;
        while(current.next != head){ //O(N)
            sum ++;
            current = current.next;
        }

        int pos = getLive(sum, m); //原链表中最后剩下节点的位置

        /*int p = 1;
        for (int i = 2; i <= sum; i++) {
            p = (p + m -1) % i + 1;
        }*/

        while(--pos != 0){
            head = head.next;
        }
        head.next = head;
        return head;
    }

    /**
     * 有i个节点的链表中最后剩下节点的位置 O(N)
     * @param i 节点总数
     * @param m 报到的数
     * @return 原链表中最后剩下节点的位置
     */
    public static int getLive(int i, int m){
        if(i == 1){
            return 1;
        }
        return (getLive(i-1, m) + m - 1)%i + 1;
    }

    public static void main(String[] args) {
        Node n1 = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        Node n4 = new Node(4);
        Node n5 = new Node(5);
        n1.next=n2;
        n2.next=n3;
        n3.next=n4;
        n4.next=n5;
        n5.next=n1;
        //System.out.println(josephus1(n1, 3).data); O(MxN)
        System.out.println(josephus2(n1, 3).data); //O(N)

    }

    static class Node{
        int data;
        Node next;

        public Node(int data) {
            this.data = data;
        }
    }
}
