package 数据结构.NowcoderZuo.day0807;

public class P1_FindUpMedian {
    public int getUpMedian(int[] arr1, int[] arr2){
        if(arr1 == null || arr2 == null || arr1.length != arr2.length)
            throw new RuntimeException("输入无效");

        int start1 = 0;
        int end1 = arr1.length-1;
        int start2 = 0;
        int end2 = arr2.length-1;
        int mid1 = 0;
        int mid2 = 0;

        while(start1 < end1){
            mid1 = (start1+end1)/2;
            mid2 = (start2+end2)/2;
            if(arr1[mid1] > arr2[mid2]){
                if((end1-start1+1)%2==0){
                    start2 = mid2 + 1;
                }else {
                    start2 = mid2;
                }
                end1 = mid1;
            }else if(arr1[mid1] < arr2[mid2]){
                if((end1-start1+1)%2==0){
                    start1 = mid1 + 1;
                }else {
                    start1 = mid1;
                }
                end2 = mid2;
            }else {
                return arr1[mid1];
            }
        }

        return Math.min(arr1[start1], arr2[start2]);

    }

    public static void main(String[] args) {
        System.out.println(new P1_FindUpMedian().getUpMedian(new int[]{1,2,3,4}, new int[]{3,4,5,6}));
        System.out.println(new P1_FindUpMedian().getUpMedian(new int[]{0,1,2}, new int[]{3,4,5}));
    }
}
