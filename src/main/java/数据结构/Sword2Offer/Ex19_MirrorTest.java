package 数据结构.Sword2Offer;

import org.junit.Test;

/**
 * 二叉树镜像
 */
public class Ex19_MirrorTest {
    @Test
    public void mirror() throws Exception {
        TreeNode root = new TreeNode(8);
        TreeNode n1 = new TreeNode(6);
        TreeNode n2 = new TreeNode(10);
        TreeNode n3 = new TreeNode(5);
        TreeNode n4 = new TreeNode(7);
        TreeNode n5 = new TreeNode(9);
        TreeNode n6 = new TreeNode(11);
        root.left = n1;
        root.right = n2;
        n1.left = n3;
        n1.right = n4;
        n2.left = n5;
        n2.right = n6;

        /*Ex19_Mirror e = new Ex19_Mirror();
        e.Mirror(root);
        e.preOrder(root);*/
    }




}