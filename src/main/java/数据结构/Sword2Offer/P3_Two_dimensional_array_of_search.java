package 数据结构.Sword2Offer;

/**
 *  二维数组查找（从右上角或者左下角开始寻找）
 *
缩小查找范围

 1  2  8  9
 2  4  9  12
 4  7  10  13
 6  8  11  15

 */
public class P3_Two_dimensional_array_of_search {
    //O(m+n)的时间复杂度，O(1)的空间复杂度
    /**
     *  返回结果
     * @param array Array
     * @param target 要查找的数
     * @return
     */
    public static boolean find(int[][] array, int target){
        if(array == null) return false;

        int row = 0; //第几行
        int col = array[0].length-1; //第几列

        while(row <= array.length-1 && col >= 0){ //查找
            if(array[row][col] == target){ //找到
                return true;
            }else if(array[row][col] > target){ //当前数值比要查找的数值target大，则向左寻找
                col --;
            }else {  //当前数值比要寻找的target小，则向下寻找
                row ++;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        int[][] array = {
                {1,2,8,9},
                {2,4,9,12},
                {4,7,10,13},
                {6,8,11,15}
        };

        System.out.println(find(array, 7));
        System.out.println(find(array, 5));
    }
}
