package 数据结构.sort;

import java.util.Stack;

/**
 * 二叉树非递归遍历
 *
 * @author Jay
 */
public class BSTIteration2 {

    // 前序遍历
    public static void preOrder(TreeNode root) {
        if(root == null) return;

        Stack<TreeNode> s  = new Stack<>();
        s.push(root);

        while(!s.empty()) {
            TreeNode node = s.pop();
            System.out.println(node.val);

            if(node.right != null) s.push(node.right);
            if(node.left != null) s.push(node.left);
        }

    }

    // 中序遍历
    public static void inOrder(TreeNode root) {
        if(root == null) return;

        Stack<TreeNode> s = new Stack<>();
        s.push(root);

        while(!s.empty()) {
            TreeNode node = s.peek();
            while(node.left != null) {
                s.push(node.left);
                node = node.left;
            }

            while(!s.empty()) {
                TreeNode n = s.pop();
                System.out.println(n.val);
                if(n.right != null) {
                    s.push(n.right);
                    break;
                }
            }
        }

    }

    // 后续遍历
    public static void postOrder(TreeNode root) {
        if(root == null) return;

        Stack<TreeNode> s = new Stack<>();
        s.push(root);

        TreeNode lastPop = null;
        while(!s.empty()) {
            TreeNode node = s.peek();
            while(node.left != null) {
                s.push(node.left);
                node = node.left;
            }

            while(!s.empty()) {
                TreeNode n = s.peek();

                if (lastPop == n.right || n.right == null){
                    lastPop = s.pop();
                    System.out.println(lastPop.val);
                } else if (n.right != null) {
                    s.push(n.right);
                    break;
                }
            }
        }
    }

    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        public TreeNode(int val) {
            this.val = val;
        }
    }

}
