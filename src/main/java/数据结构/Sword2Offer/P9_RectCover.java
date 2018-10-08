package 数据结构.Sword2Offer;

/**
 * 矩阵覆盖
 */
public class P9_RectCover {
    public int RectCover(int target) {

        if(target == 0){
            return 0;
        }else if(target == 1){
            return 1;
        }else if(target == 2){
            return 2;
        }else{
            return RectCover(target-1) + RectCover(target-2);
        }

    }
}
