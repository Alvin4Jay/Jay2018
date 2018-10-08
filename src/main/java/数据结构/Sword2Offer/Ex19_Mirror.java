package 数据结构.Sword2Offer;

/**
 *  二叉树镜像
 */
public class Ex19_Mirror {

    public void Mirror(TreeNode root) {
        if(root == null) return;
        if(root.left == null && root.right == null) return;

        TreeNode temp = root.left;
        root.left = root.right;
        root.right = temp;

        Mirror(root.left);
        Mirror(root.right);

    }

    public void preOrder(TreeNode root){
        System.out.println(root.val);
        if(root.left != null){
            preOrder(root.left);
        }
        if(root.right!=null){
            preOrder(root.right);
        }
    }


}
