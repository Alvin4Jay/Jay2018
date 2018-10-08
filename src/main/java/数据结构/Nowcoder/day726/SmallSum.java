package 数据结构.Nowcoder.day726;

/**
 * 计算数组的小和
 */
public class SmallSum {
    //计算小和
    public int calcMonoSum(int[] A, int n) {
        if(A == null || A.length == 0 || n <= 0) return 0;

        int[] help = new int[n];
        return sort(A, help, 0, n-1);
    }
    //归并排序
    public int sort(int[] A, int[] help, int left, int right){
        if(right<=left) return 0; //只剩一个数，小和为0
        int mid = (left+right)/2;
        int leftSum = sort(A, help, left, mid);
        int rightSum = sort(A,help, mid+1, right);
        return leftSum + rightSum + merge(A, help, left, mid, right);
    }
    //原地归并
    public int merge(int[] A, int[] help,  int left, int mid, int right){
        int smallSum = 0; //小和
        int i= left,j = mid+1;
        for (int k = left; k <= right; k++) {
            help[k] = A[k];
        }
        for (int k = left; k <= right; k++) {
            if(i > mid) A[k] = help[j++]; //超界
            else if(j > right) A[k] = help[i++]; //超界
            else if(help[i] <= help[j]){
                smallSum += help[i]*(right-j+1); //加入这一行
                A[k] = help[i++];
            }else {
                A[k] = help[j++];
            }
        }
        return smallSum;
    }

    public static void main(String[] args) {
        int[] a = new int[]{3, 1, 2, 4, 6, 2, 7, 8, 1 };
        System.out.println(new SmallSum().calcMonoSum(a, a.length));
    }
}
