package 数据结构.Tree.BinarySearchTree;

import static org.junit.Assert.assertEquals;

/**
 *  树的子结构

 思路：1.从A树根节点开始遍历，如果碰到节点值与要判断的子树B根节点值相同，那么递归判断此时A树的子树与B树是否相同，相同则结束，返回true
      2.否则，递归的遍历A树的左右子树，直到结束，返回结果值。

 */
public class HasSubTree {

    public static boolean hasSubTree(Node A, Node B){
        boolean result = false;
        if(A != null && B  != null){
            if(A.val == B.val)  result = doesTree1HaveTree2(A, B); //遇到A B值相等，判断一次
            if(!result){
                result = hasSubTree(A.left, B); // 在左子树中继续查找
            }
            if(!result){
                result = hasSubTree(A.right, B);// 在右子树中继续查找
            }
        }
        return result;
    }

    private static boolean doesTree1HaveTree2(Node tree1, Node tree2){
        if(tree2 == null){
            return true;  //tree2先遍历完了，不管tree1如何，都应该返回true
        }

        if(tree1 == null){
            return false; //tree1先没了，则应该返回false
        }

        if(tree1.val != tree2.val){  //第四种情况，都存在，但不相等，返回false
            return false;
        }

        //相等，此轮判断结束，继续递归遍历
        return doesTree1HaveTree2(tree1.left, tree2.left) && doesTree1HaveTree2(tree1.right, tree2.right);
    }

    private static class Node{
        int val;
        Node left;
        Node right;

        public Node(int val) {
            this.val = val;
        }
    }

    public static void main(String[] args) {
        Node root1 = new Node(8);
        Node node1 = new Node(8);
        Node node2 = new Node(7);
        Node node3 = new Node(9);
        Node node4 = new Node(2);
        Node node5 = new Node(4);
        Node node6 = new Node(7);
        root1.left = node1;
        root1.right = node2;
        node1.left = node3;
        node1.right = node4;
        node4.left = node5;
        node4.right = node6;

        Node root2;
        root2 = new Node(8);
        root2.left = new Node(9);
        root2.right = new Node(2);

        boolean result = HasSubTree.hasSubTree(root1,root2);
        System.out.println(result);
        assertEquals(true, result);
    }
}
