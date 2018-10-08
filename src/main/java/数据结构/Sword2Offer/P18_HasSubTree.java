package 数据结构.Sword2Offer;

/**
 *  树的子结构(递归)
 *  输入两棵二叉树A，B，判断B是不是A的子结构。（ps：我们约定空树不是任意一个树的子结构）
 */
public class P18_HasSubTree {
    public boolean HasSubtree(TreeNode root1,TreeNode root2) {
        boolean result = false;

        /*
         * 如果root1、root2两者中有一个为空，返回false
         */
        if(root1!=null && root2!=null){
            if(root1.val == root2.val){ //如果root1的val与root2的val相等
                result = doesTree1HasTree2(root1, root2);
            }
            if(!result){ //如果root1的val与root2的val不相等，或者相等但是不是子结构
                result = HasSubtree(root1.left, root2);  //对root1的左子树进行分析
            }
            if(!result){
                result = HasSubtree(root1.right, root2); //对root1的右子树进行分析
            }
        }
        return result;
    }

    private boolean doesTree1HasTree2(TreeNode p1, TreeNode p2){
        if(p2 == null){ //如果p2先变为null，返回true
            return true;
        }
        if(p1 == null){ //如果p1先变为null且p2不为null
            return false;
        }

        if(p1.val != p2.val){ //两个值不等
            return false;
        }
        //对两者的左右子树进行验证
        return doesTree1HasTree2(p1.left, p2.left) && doesTree1HasTree2(p1.right, p2.right);
    }
}
