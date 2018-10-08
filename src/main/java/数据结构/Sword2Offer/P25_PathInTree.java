package 数据结构.Sword2Offer;

import java.util.ArrayList;
import java.util.Stack;

/**
 * 二叉树中和为某一值的路径(从根节点到叶子结点)
 */
public class P25_PathInTree {
    public ArrayList<ArrayList<Integer>> FindPath(TreeNode root, int target) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        if(root == null) return result;

        Stack<Integer> path = new Stack<Integer>(); //路径
        int sum = 0; //和

        find(result, path, sum, target, root);

        return result;
    }

    /**
     * 递归函数(前序遍历)
     * @param result 结果集
     * @param path 路径
     * @param sum 和
     * @param target 目标数值
     * @param node 本次节点
     */
    public void find(ArrayList<ArrayList<Integer>> result, Stack<Integer> path,
                     int sum, int target, TreeNode node){
        path.push(node.val); //计算路径、和
        sum += node.val; //int sum1 = sum + node.val;后面改为sum1
        //找到
        if(node.left == null && node.right == null && sum == target){
            ArrayList<Integer> temp = new ArrayList<Integer>(); //得到的路径不用逆序
            for (Integer i : path) {
                temp.add(i);
            }
            result.add(temp);
        }
        //如果左子树不为空，继续检查
        if(node.left != null){
            find(result, path, sum, target, node.left);
        }
        //右子树不为空，继续检查
        if(node.right != null){
            find(result, path, sum, target, node.right);
        }
        //本节点检查结束之后，路径需要删除本节点，sum需要减去本节点的值(局部变量)
        path.pop();
    }
}