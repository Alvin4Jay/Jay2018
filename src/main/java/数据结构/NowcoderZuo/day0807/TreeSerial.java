package 数据结构.NowcoderZuo.day0807;

import java.util.LinkedList;
import java.util.Queue;

public class TreeSerial {

    //树的序列化(先序遍历)
    public String serialByPre(Node head){
        if(head == null) {
            return "#!";
        }
        String res = head.value + "!";
        res += serialByPre(head.left);
        res += serialByPre(head.right);
        return res;
    }

    //树的反序列化(按照先序遍历的顺序)
    public Node reconByPreString(String preStr){
        String[] values = preStr.split("\\!");
        Queue<String> queue = new LinkedList<>();
        for (int i = 0; i < values.length; i++) {
            queue.offer(values[i]);
        }
        return reconPreOrder(queue);
    }

    public Node reconPreOrder(Queue<String> queue){
        String value = queue.poll();
        if(value.equals("#")){
            return null;
        }
        Node head = new Node(Integer.valueOf(value));
        head.left = reconPreOrder(queue);
        head.right = reconPreOrder(queue);
        return head;
    }

    public class Node{
        public int value;
        public Node left;
        public Node right;

        public Node(int value) {
            this.value = value;
        }
    }
}
