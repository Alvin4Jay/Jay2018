package 数据结构.Sword2Offer;

/**
 * 二叉搜索树的后序遍历序列验证
 */
public class P24_VerifySquenceOfBST {
    public boolean VerifySquenceOfBST(int [] sequence) {
        if(sequence == null || sequence.length == 0) return false;

        return Verify(sequence, 0, sequence.length-1);
    }

    public boolean Verify(int[] sequence, int start, int end){
        if(start == end) return true;

        int pos = start; //定位小于根节点pos2的位置

        for(; pos < end; pos++){
            if(sequence[pos] > sequence[end]) break;
        }

        //验证右半部分是否合法
        for (int i = pos; i < end; i++) {
            if(sequence[i] < sequence[end]) return false;
        }

        boolean left = true; //验证左子树
        boolean right = true; //验证右子树
        if(pos > start){
            left = Verify(sequence, start, pos-1);
        }
        if(pos < end){
            right = Verify(sequence, pos, end-1);
        }
        return left && right;
    }
}
