package 数据结构.LeetCode.BFS;

import java.util.LinkedList;
import java.util.Queue;

/**
 Given a binary tree, find its minimum depth.

 The minimum depth is the number of nodes along the shortest path from the root node
 down to the nearest leaf node.
 */
//宽度优先搜索
public class Ex111_Minimum_Depth_of_Binary_Tree {
    public int minDepth(TreeNode root) {
        if(root == null) return 0;

        Queue<TreeNode> queue = new LinkedList<TreeNode>();
        TreeNode cur = null;
        int minH = 0;
        queue.offer(root);
        while(!queue.isEmpty()){
            minH ++;
            int size = queue.size();
            for (int i = 0; i < size; i++) {  //一层一层的检查
                cur = queue.poll();
                if(cur.left == null && cur.right == null){
                    return minH;
                }
                if(cur.left != null){
                    queue.offer(cur.left);
                }
                if(cur.right != null){
                    queue.offer(cur.right);
                }
            }
        }
        return minH;
    }

    public class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode(int x) { val = x; }
    }
}
