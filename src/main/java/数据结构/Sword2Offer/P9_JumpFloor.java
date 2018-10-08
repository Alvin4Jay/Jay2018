package 数据结构.Sword2Offer;

/**
 * 青蛙跳 每次只能跳1步或者2步，N级台阶
 */
public class P9_JumpFloor {
    //实质是斐波那契数列
    /*
    F(n)=F(n-1)+F(n-2);
    F(0)=0;
    F(1)=1;
    F(2)=2;
     */
    public int JumpFloor(int target) {

        if(target == 0){
            return 0;
        }else if(target == 1){
            return 1;
        }else if(target == 2){
            return 2;
        }else{
            return JumpFloor(target-1) + JumpFloor(target-2);
        }

    }
}
