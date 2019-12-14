package 数据结构.sort;

import java.util.Stack;

/**
 * 二叉树非递归遍历
 *
 * @author Jay
 */
public class BSTIteration {

    // 前序遍历
    public static void preOrder(TreeNode root) {
        if (root == null) {
            return;
        }

        Stack<TreeNode> s = new Stack<>();
        s.push(root);

        while(!s.empty()) {
            TreeNode cur = s.pop();
            System.out.println(cur.val);

            if (cur.right != null) {
                s.push(cur.right);
            }

            if (cur.left != null) {
                s.push(cur.left);
            }
        }
    }

    // 中序遍历
    public static void inOrder(TreeNode root) {
        if (root == null) {
            return;
        }

        Stack<TreeNode> s = new Stack<>();
        s.push(root);

        while (!s.empty()) {
            // 找到最左节点
            TreeNode p = s.peek();
            while (p.left != null) {
                s.push(p.left);
                p = p.left;
            }

            while (!s.empty()){
                TreeNode n = s.pop();
                System.out.println(n.val);
                // 右节点不为空，退出本循环
                if (n.right != null) {
                    s.push(n.right);
                    break;
                }
            }
        }
    }

    // 后续遍历
    public static void postOrder(TreeNode root) {
        if (root == null) {
            return;
        }

        Stack<TreeNode> s = new Stack<>();
        s.push(root);

        TreeNode lastPop = null; // 记录上一次出栈的节点
        while (!s.empty()) {

            TreeNode p = s.peek();
            while (p.left != null) {
                s.push(p.left);
                p = p.left;
            }

            while (!s.empty()) {
                p = s.peek();

                if (lastPop == p.right || p.right == null) {
                    lastPop = s.pop();
                    System.out.println(lastPop.val + " ");
                } else if (p.right != null) {
                    s.push(p.right);
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


    public static void main(String[] args) {
        TreeNode root = new TreeNode(5);
        TreeNode l1 = new TreeNode(1);
        TreeNode r1 = new TreeNode(6);
        root.left = l1;
        root.right = r1;

        TreeNode r2 = new TreeNode(10);
        r1.right = r2;
        postOrder(root);
    }

}
