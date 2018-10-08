package 数据结构.Sword2Offer;

/**
 *  二叉树的镜像(前序遍历+递归)
 */
public class P19_BSTMirror {
    public void Mirror(TreeNode root) {
        if(root == null) return;
        //if(root.left == null && root.right == null) return;

        TreeNode temp = root.left;
        root.left = root.right;
        root.right = temp;

        Mirror(root.left);
        Mirror(root.right);

        /*
        if(root == null) return ;
        if(root.left != null || root.right!=null){
            TreeNode tmp = root.left;
            root.left = root.right;
            root.right = tmp;
        }
        if(root.left != null) Mirror(root.left);
        if(root.right != null) Mirror(root.right);
        * */

    }

    static class TreeNode {
        int val = 0;
        TreeNode left = null;
        TreeNode right = null;

        public TreeNode(int val) {
            this.val = val;
        }
    }
}