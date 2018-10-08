package 数据结构.Tree.BinarySearchTree;

import java.util.Stack;

/**
 *  二叉树中和为某一值的路径（规定从根结点到叶子结点，无子节点）
 *  前序遍历
 */
public class FindPathInBST {
    public static void findPath(Node root, int expectedSum){
        if(root == null) return;

        Stack<Integer> path = new Stack<Integer>(); //路径
        int currentSum = 0;  //目前之和
        findPath(root, expectedSum, path, currentSum);
    }
    private static void findPath(Node node, int expectedSum, Stack<Integer> path, int currentSum){
        path.push(node.val);
        currentSum += node.val;
        //功能
        boolean isLeafNode = (node.left == null) && (node.right == null);
        if(currentSum == expectedSum && isLeafNode){ //找到
            System.out.println("Demo.A path is found: ");
            for (Integer integer : path) {
                System.out.print(integer + " ");
            }
            System.out.print("\r\n");
        }

        if(node.left != null){
            findPath(node.left, expectedSum, path, currentSum);
        }
        if(node.right != null){
            findPath(node.right, expectedSum, path, currentSum);
        }

        path.pop();//path删除当前节点值，currentSum不用减去，因为currentSum是局部变量，只在这个函数内有效
    }

    public static void main(String[] args) {
        Node root = new Node(10);
        Node n1 = new Node(5);
        Node n2 = new Node(12);
        Node n3 = new Node(4);
        Node n4 = new Node(7);
        root.left = n1;root.right=n2;
        n1.left = n3;n1.right = n4;
        findPath(root, 22);
    }

    private static class Node{
        int val;
        Node left;
        Node right;

        public Node(int val) {
            this.val = val;
        }
    }
}
