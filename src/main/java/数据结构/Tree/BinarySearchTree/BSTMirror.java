package 数据结构.Tree.BinarySearchTree;

import java.util.LinkedList;
import java.util.Queue;

/**
 *  二叉树的镜像
 */
public class BSTMirror {
    public static void mirror(TreeNode root) {
        if(root == null) return;
        if(root.left == null && root.right == null) return;

        TreeNode temp = root.left;
        root.left = root.right;
        root.right = temp;

        mirror(root.left);
        mirror(root.right);
    }

    static class TreeNode {
        int val = 0;
        TreeNode left = null;
        TreeNode right = null;

        public TreeNode(int val) {
            this.val = val;
        }
    }

    public static void main(String[] args) {

        TreeNode n1 = new TreeNode(8);
        TreeNode n2 = new TreeNode(6);
        TreeNode n3 = new TreeNode(10);
        TreeNode n4 = new TreeNode(5);
        TreeNode n5 = new TreeNode(7);
        TreeNode n6 = new TreeNode(9);
        TreeNode n7 = new TreeNode(11);
        n1.left = n2;n1.right=n3;
        n2.left=n4;/*n2.right=n5;*/n3.left=n6;n3.right=n7;

        traveralByLevel(n1);
        mirror(n1);
        System.out.println("\r\n*********************");
        traveralByLevel(n1);

    }

    //层次遍历
    public static void traveralByLevel(TreeNode n){
        if(n == null) return;

        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        queue.offer(n);

        while(!queue.isEmpty()){
            TreeNode node = queue.poll();
            System.out.print(node.val + " ");
            if(node.left != null) queue.offer(node.left);
            if(node.right != null) queue.offer(node.right);
        }
    }
}
