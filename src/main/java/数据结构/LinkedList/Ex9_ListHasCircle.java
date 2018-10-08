package 数据结构.LinkedList;

import java.util.HashMap;
import java.util.Map;

/**
 * 单链表是否有环
 */
public class Ex9_ListHasCircle {

    /**
     * 判断单链表是否有环，有环返回进入环的第一个节点的位置，否则返回null。
     * @param node
     * @return 时间复杂度为O(N)，空间复杂度为O(N) 利用HashMap
     */
    public Node hasLoop(Node node){
        if(node == null || node.next == null) return null;

        Map<Node, Integer> map = new HashMap<>();

        while(node != null){
            if(map.containsKey(node)){ //有环
                return node;
            }
            map.put(node, 1);
            node= node.next;
        }
        return null;
    }

    /**
     * 快慢指针方法
     * @param head
     * @return 进入环的第一个节点
    利用快慢指针遍历链表，确定是否有环。快指针走两步，慢指针走一步，如果快指针指向了null，表示无环；
    如果快指针和慢指针最后指向的同一个结点，表示链表中有环。再来确定第一个进入环的位置，当前面快指针
    和慢指针指向同一个结点时，快指针重新指向链表头结点，此后，慢指针和快指针同步以一个步伐的速度前进，
    当再一次相遇时，这个结点就是第一次进入环的结点。 （可以证明）
     */
    public Node hasLoop2(Node head){
        if(head == null || head.next == null) return null; //只有一个或者没有

        Node fast = head;
        Node slow = head;

        while(fast != null && fast.next != null){
            fast = fast.next.next;//走两步
            slow = slow.next; //走一步
            if(fast == slow) break;  //有环，相遇
        }
        //无环
        if(fast == null || fast.next == null){
            return null;
        }
        //有环
        fast = head; //重新指向头结点
        while(fast != slow){
            fast = fast.next; //每次同时走一步
            slow = slow.next;
        }
        return fast; //相遇时即是进入环的的第一个节点
    }

    private static class Node{
        int value;
        Node next;
        Node(int value){
            this.value = value;
        }
    }
}

