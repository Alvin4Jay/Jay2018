package 数据结构.Sword2Offer;

/**
 *  在O(1)时间内删除链表节点（多种边界情况）
 */
public class P13_DeleteNode {
    /**
     * 删除
     * @param head 头结点
     * @param toBeDelete 待删除节点
     */
    public void deleteNode(Node head, Node toBeDelete){
        if(head == null || toBeDelete == null){
            return;
        }
        if(head.next == null && head == toBeDelete){ //只有一个节点
            head = null;
            toBeDelete = null;
        }else if(toBeDelete.next == null){ //删除的是尾节点，遍历O(n)
            Node current = head;
            while(current.next != toBeDelete){
                current = current.next;
            }
            current.next = null;
            toBeDelete = null;
        }else {   //O(1)时间的删除节点操作
            Node next = toBeDelete.next; //待删除节点的下一个节点
            toBeDelete.value = next.value; //复制next节点的value到toBeDelete的节点中
            toBeDelete.next = next.next; //删除next节点
        }
    }

    static class Node{
        int value;
        Node next;

        public Node(int value) {
            this.value = value;
        }
    }

    public static void main(String[] args) {
        Node head = new Node(1);
        Node n2 = new Node(2);
        Node n3 = new Node(3);
        head.next = n2;
        n2.next = n3;

        new P13_DeleteNode().deleteNode(head, n2);
        System.out.println(head.next.value);
    }
}
