package 数据结构.Tree.BinarySearchTree;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 从上往下打印二叉树（Queue）
 */
public class PrintFromTopToBottom {

    public static void printFromTopToBottom(Node root){
        if(root == null) return;

        Queue<Node> queue = new LinkedList<Node>();
        queue.offer(root);

        while(!queue.isEmpty()){
            Node c= queue.poll();
            System.out.print(c.val + " ");

            if(c.left != null) queue.offer(c.left);
            if(c.right != null) queue.offer(c.right);
        }

    }

    private static class Node{
        int val;
        Node left;
        Node right;

        public Node(int val) {
            this.val = val;
        }
    }

    public static void main(String[] args) {
        Node n1 = new Node(8);
        Node n2 = new Node(6);
        Node n3 = new Node(10);
        Node n4 = new Node(5);
        Node n5 = new Node(7);
        Node n6 = new Node(9);
        Node n7 = new Node(11);
        n1.left=n2;n1.right=n3;
        n2.left=n4;n2.right=n5;n3.left=n6;n3.right=n7;

        printFromTopToBottom(n1);

    }
}
