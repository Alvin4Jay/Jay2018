package 数据结构.Sword2Offer;

/**
 * 旋转数组的最小元素
 */
public class P8_MinNumberInRotateArray {
    //O(N)
    public static int minNumberInRotateArray1(int [] array) {
        if (array == null || array.length == 0) return 0;

        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return array[i + 1]; //两种情况 //1 0 1 1 1    3 4 5 1 2
            }

        }
        return array[0]; //一种情况 未旋转 1 2 3 4 5
    }

    //O(lgN)
    public static int minNumberInRotateArray2(int[] array) {
        if (array == null || array.length == 0) {
            return 0;
        }

        int left = 0;
        int right = array.length - 1;
        int mid = 0;

        while (array[left] >= array[right]) { //确保旋转
            if (right - left == 1) {
                mid = right; //查找结束
                break;
            }

            mid = (left + right) / 2;
            //1 0 1 1 1 // 1 1 1 0 1 顺序查找
            if (array[right] == array[left] && array[mid] == array[right]) {
                return min(array, left, right);

            }
            //二分
            if (array[mid] >= array[left]) {
                left = mid;
            } else if (array[mid] <= array[right]) {
                right = mid;
            }

        }
        return array[mid];
    }

    public static int min(int[] array, int left, int right){
        int result = array[left];
        for(int i=left+1;i<=right;i++){
            if(array[i]<result){
                result = array[i];
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(minNumberInRotateArray1(new int[]{3,4,5,1,2}));
        System.out.println(minNumberInRotateArray1(new int[]{1,0,1,1,1}));
        System.out.println(minNumberInRotateArray2(new int[]{3,4,5,1,2}));
        System.out.println(minNumberInRotateArray2(new int[]{1,0,1,1,1}));
    }
}
