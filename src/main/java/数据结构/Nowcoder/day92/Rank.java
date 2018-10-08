package 数据结构.Nowcoder.day92;

/**
 * Created by Jay on 2017/9/3
 */
public class Rank {
    public int[] getRankOfNumber(int[] A, int n) {
        int[] resu = new int[n];
        resu[0]=  0;
        for (int i = 0; i < n; i++) {
            int index = bs(A,0, i-1, A[i]);
            resu[i] = index;
            int now = A[i];
            for (int j = i; j >  index; j++) {
                A[j] = A[j-1];
            }
            A[index] = now;
        }
        return resu;
    }

    private int bs(int[] a, int first, int lsat, int x) {
        int low = first, high = lsat;
        while (low <= high){
            int mid = (low+high)/2;
            if(a[mid]<x){
                low = mid+1;
            }else {
                high = mid-1;
            }
        }
        return low;
    }


}
