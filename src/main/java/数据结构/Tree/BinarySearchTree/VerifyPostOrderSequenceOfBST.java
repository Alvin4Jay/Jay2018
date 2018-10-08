package 数据结构.Tree.BinarySearchTree;

import java.util.Arrays;

/**
 *  验证一个整数数组是否是二叉搜索树的后序遍历序列
 */
public class VerifyPostOrderSequenceOfBST {
    public static boolean verifySequenceOfBST(int[] sequence, int length){
        //第一次判断条件
        if(sequence == null || length <=0 ){
            return false;
        }

        //第二次判断
        int root = sequence[length-1];

        int i = 0;  //找到刚开始大于rootValue的元素索引（左子树）
        for(; i<length-1 ; ++i){
            if(sequence[i] > root) break;
        }

        int j = i;  //检查右子树,从i开始
        for(; j< length-1 ; j++){
            if(sequence[j]<root) return false; //小于root元素，返回false
        }

        //第三次判断
        //验证左子树
        boolean left = true; //默认
        if(i > 0){ //存在元素,左子树总结点数
            left = verifySequenceOfBST(Arrays.copyOfRange(sequence, 0, i) , i);
        }
        //验证右子树
        boolean right = true;
        if(length-i-1 > 0){//右子树存在元素
            right = verifySequenceOfBST(Arrays.copyOfRange(sequence, i, length-1) , length-i-1);
        }

        return left && right;
    }

    public static void main(String[] args) {

        //int[] sequence = {5,7,6,9,11,10,8};
        int[] sequence = {7,4,6,5};

        boolean result = VerifyPostOrderSequenceOfBST.verifySequenceOfBST(sequence, sequence.length);
        System.out.println(result);
    }
}
