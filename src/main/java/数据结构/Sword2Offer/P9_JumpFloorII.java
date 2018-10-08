package 数据结构.Sword2Offer;

/**
 * 变态跳(可以跳上1级台阶，也可以跳上2级……它也可以跳上n级) 总共N级台阶
 */
public class P9_JumpFloorII {
    public int JumpFloorII(int target) {
        if(target == 0){
            return 0;
        }else if(target == 1){
            return 1;
        }else if(target == 2){
            return 2;
        }else {
            return 2*JumpFloorII(target-1);
        }
    }
}
