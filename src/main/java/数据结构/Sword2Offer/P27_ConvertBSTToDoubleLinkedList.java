package 数据结构.Sword2Offer;

/**
 * 二叉搜索树转为排序的双向链表
 */
public class P27_ConvertBSTToDoubleLinkedList {
    public TreeNode lastInList = null; // 以前排序好的双向链表中最后一个节点

    public TreeNode Convert(TreeNode pRootOfTree) {
        convertInRec(pRootOfTree);

        TreeNode head = lastInList;
        while(head != null && head.left != null){
            head = head.left;
        }
        return head;
    }

    /**
     * 中序遍历
     * @param node 本次节点
     */
    public void convertInRec(TreeNode node){
        if(node == null){
            return;
        }

        TreeNode current = node;

        if(current.left != null){
            convertInRec(current.left);
        }

        current.left = lastInList; //当前节点左指针指向之前最后一个节点
        if(lastInList != null){
            lastInList.right = current; //之前最后一个节点右指针指向当前节点
        }
        lastInList = current; //修改最后节点为当前节点

        if(current.right != null){
            convertInRec(current.right);
        }
    }

    public static void main(String[] args) {
        TreeNode n1 = new TreeNode(10);
        TreeNode n2 = new TreeNode(6);
        TreeNode n3 = new TreeNode(14);
        TreeNode n4 = new TreeNode(4);
        TreeNode n5 = new TreeNode(8);
        TreeNode n6 = new TreeNode(12);
        TreeNode n7 = new TreeNode(16);
        n1.left = n2;n1.right=n3;
        n2.left=n4;n2.right=n5;
        n3.left=n6;n3.right=n7;
        TreeNode res = new P27_ConvertBSTToDoubleLinkedList().Convert(n1);

        while(res != null){
            System.out.print(res.val + " ");
            res = res.right;
        }
    }
}
