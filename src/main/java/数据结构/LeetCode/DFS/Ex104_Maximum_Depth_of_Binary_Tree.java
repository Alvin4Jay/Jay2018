package 数据结构.LeetCode.DFS;
/*
Given a binary tree, find its maximum depth.

The maximum depth is the number of nodes along
the longest path from the root node down to the farthest leaf node.
 */
public class Ex104_Maximum_Depth_of_Binary_Tree {
    public int maxDepth(TreeNode root) {
        int maxL = 0, hL = 0, hR = 0;
        //后续遍历
        if(root != null){
            hL = maxDepth(root.left);
            hR = maxDepth(root.right);
            maxL = (hL > hR ? hL: hR) + 1;
        }
        return maxL;
    }

    public class TreeNode {
      int val;
      TreeNode left;
      TreeNode right;
      TreeNode(int x) { val = x; }
    }
}
