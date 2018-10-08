package 数据结构.Nowcoder.dp;

import java.util.Scanner;

/**
 * 滑雪(动态规划)
 */
public class Poj1088 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int maxLength = 0; //最大长度
        while(sc.hasNextInt()){
            int M = sc.nextInt();
            int N = sc.nextInt();
            int[][] height = new int[M + 2][N + 2]; //防止出界
            int[][] length = new int[M + 2][N + 2]; //防止出界
            for (int i = 1; i <= M; i++) {
                for (int j = 1; j <= N; j++) {
                    height[i][j] = sc.nextInt();
                }
            }

            for (int i = 1; i <= M; i++) {
                for (int j = 1; j <= N; j++) {
                    length[i][j] = recursion(height, length, i, j, M, N); //求每个位置的最大长度
                    if(maxLength < length[i][j]){
                        maxLength = length[i][j];
                    }
                }
            }
            System.out.println(maxLength);
            maxLength = 0;
        }
    }

    /**
     * 递归求出i,j位置的最长路径
     * @param height 每个点的高度
     * @param length 已经计算出的长度
     * @param i x坐标
     * @param j y坐标
     * @param M 输入行数
     * @param N 输入列数
     * @return x,y位置的最长路径
     */
    public static int recursion(int[][] height, int[][] length, int i, int j, int M, int N){
        if(height == null || height.length == 0 || height[0].length == 0){
            return 0;
        }
        if(i < 1 || j < 1 || i > M || j > N){ //边界条件M+2 , N+2
            return 0;
        }

        if(length[i][j] != 0){ //已经计算过
            return length[i][j];
        }

        int temp = 0, max = 0;
        //left
        if(height[i][j] > height[i-1][j]){
            temp = recursion(height, length, i-1, j, M, N);
            max = Math.max(max, temp);
        }
        //right
        if(height[i][j] > height[i+1][j]){
            temp = recursion(height, length, i+1, j, M, N);
            max = Math.max(max, temp);
        }
        //up
        if(height[i][j] > height[i][j-1]){
            temp = recursion(height, length, i, j-1, M, N);
            max = Math.max(max, temp);
        }
        //down
        if(height[i][j] > height[i][j+1]){
            temp = recursion(height, length, i, j+1, M, N);
            max = Math.max(max, temp);
        }
        return max + 1; //max+1加上自己
    }
}
