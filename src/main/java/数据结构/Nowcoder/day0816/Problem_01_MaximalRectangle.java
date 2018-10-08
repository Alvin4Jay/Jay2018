package 数据结构.Nowcoder.day0816;

import java.util.Stack;
//单调栈O(MxN)
public class Problem_01_MaximalRectangle {

	public static int maxRecSize(int[][] map) {
		if (map == null || map.length == 0 || map[0].length == 0) {
			return 0;
		}//最大面积
		int maxArea = 0;
		int[] height = new int[map[0].length]; //直方图数组
		for (int i = 0; i < map.length; i++) { //必须以每行为底的情况下，求出每个最大矩阵比较
			for (int j = 0; j < map[0].length; j++) {
				height[j] = map[i][j] == 0 ? 0 : height[j] + 1; //直方图
			}
			maxArea = Math.max(maxRecFromBottom(height), maxArea);
		}
		return maxArea;
	}
	//单调栈求最大矩阵
	public static int maxRecFromBottom(int[] height) {
		if (height == null || height.length == 0) {
			return 0;
		}
		int maxArea = 0;
		Stack<Integer> stack = new Stack<Integer>(); //存放下标
		for (int i = 0; i < height.length; i++) {
			while (!stack.isEmpty() && height[i] <= height[stack.peek()]) {
				int j = stack.pop();
				int k = stack.isEmpty() ? -1 : stack.peek();
				int curArea = (i - k - 1) * height[j];
				maxArea = Math.max(maxArea, curArea);
			}
			stack.push(i);
		}
		while (!stack.isEmpty()) {
			int j = stack.pop();
			int k = stack.isEmpty() ? -1 : stack.peek();
			int curArea = (height.length - k - 1) * height[j]; //右边没有比他小的数
			maxArea = Math.max(maxArea, curArea);
		}
		return maxArea;
	}

	public static void main(String[] args) {
		int[][] map = {
				       {1, 0, 1, 1},
				       {1, 1, 1, 1},
				       {1, 1, 1, 0}
				      };
		System.out.println(maxRecSize(map));
	}

}

class Solution {
	public static int maximalRectangle(char[][] matrix) {
		if(matrix == null || matrix.length == 0 || matrix[0].length == 0){
			return 0;
		}

		int M = matrix.length;
		int N = matrix[0].length;
		int[][] map = new int[M][N];
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				map[i][j] = Integer.parseInt(String.valueOf(matrix[i][j]));
			}
		}

		int[] height = new int[N];
		int maxArea = 0;
		for(int i=0;i<M;i++){
			for(int j=0;j<N;j++){
				height[j] = map[i][j]>0 ? height[j]+1 : 0 ;
			}
			maxArea = Math.max(maxArea, calMaxArea(height));
		}
		return maxArea;
	}

	public static int calMaxArea(int[] height){
		if(height == null || height.length == 0){
			return 0;
		}

		int maxArea = 0;
		int n = height.length;
		Stack<Integer> stack = new Stack<Integer>();

		for(int i=0; i< height.length; i++){

			while(!stack.isEmpty() && height[i] < height[stack.peek()]){
				int j = stack.pop(); //弹出元素位置
				int k = stack.isEmpty() ? -1 : stack.peek() ;
				maxArea = Math.max(maxArea, (i-k-1)*height[j]);
			}
			stack.push(i);
		}

		while(!stack.isEmpty()){
			int j = stack.pop();
			int k = stack.isEmpty() ? -1 : stack.peek() ;
			maxArea = Math.max(maxArea, (n-k-1)*height[j]);
		}
		return maxArea;
	}

	public static void main(String[] args) {
		char[][] a = {	{'1','0','1','0','0'},
						{'1','0','1','1','1'},
						{'1','1','1','1','1'},
						{'1','0','0','1','0'}};
		System.out.println(maximalRectangle(a));
	}

}
