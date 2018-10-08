package 数据结构.Sword2Offer;

/**
 *  二叉树A是否包含二叉树B
 */
public class Ex18_SubTree {

    public static boolean hasSubTree(TreeNode root1, TreeNode root2){
        boolean result = false;

        if(root1!=null && root2!=null){
            if(root1.val == root2.val){
                result = doesTree1HasTree2(root1, root2);  //root1中找到根节点
            }
            if(!result){
                result = hasSubTree(root1.left, root2);
            }
            if(!result){
                result = hasSubTree(root1.right, root2);
            }
        }
        return result;
    }

    private static boolean doesTree1HasTree2(TreeNode p1, TreeNode p2){
        if(p2 == null){  //递归过程中，p2为空，表明A包含B树
            return true;
        }
        if(p1 == null){ //p2不为空，p1为空，表明A树不包含B树
            return false;
        }

        if(p1.val != p2.val){  //p1/p2都不为空，且值不相等
            return false;
        }
        return doesTree1HasTree2(p1.left, p2.left) && doesTree1HasTree2(p1.right, p2.right);
    }

}
 class TreeNode {
    int val = 0;
    TreeNode left = null;
    TreeNode right = null;

    public TreeNode(int val) {
        this.val = val;
    }
}

