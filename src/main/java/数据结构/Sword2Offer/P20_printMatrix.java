package 数据结构.Sword2Offer;

import java.util.ArrayList;

/**
 * 顺时针打印矩阵
 */
public class P20_printMatrix {
    public ArrayList<Integer> printMatrix(int [][] matrix) {
        ArrayList<Integer> result = new ArrayList<Integer>();
        if(matrix == null) return result;
        int rows = matrix.length, cols = matrix[0].length;
        if(cols <= 0 || rows <= 0) return result;

        int start = 0;

        while(2*start < cols && 2*start < rows){  //退出条件
            print(result, matrix, start, cols, rows);
            start ++;
        }

        return result;
    }

    private void print(ArrayList<Integer> result, int[][] matrix,
                       int start, int cols, int rows){
        int endX = cols - 1 - start;
        int endY = rows - 1 -start;
        //从左至右
        if(start <= endX){
            for (int i = start; i <= endX; i++) {
                result.add(matrix[start][i]);
            }
        }
        //从上到下
        if(endY > start){
            for (int i = start+1; i <= endY; i++) {
                result.add(matrix[i][endX]);
            }
        }
        //从右到左
        if(endY > start && start < endX) {
            for (int i = endX-1; i >= start; i--) {
                result.add(matrix[endY][i]);
            }
        }
        //从下至上
        if(start < endX && start < endY-1){
            for (int i = endY-1; i >= start+1; i--) {
                result.add(matrix[i][start]);
            }
        }
    }
}
