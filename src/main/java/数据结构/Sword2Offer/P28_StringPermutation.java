package 数据结构.Sword2Offer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * 字符串的全排列(考虑重复，递归实现)
 */
public class P28_StringPermutation {
    public ArrayList<String> Permutation(String str) {
        ArrayList<String> result = new ArrayList<String>();
        if(str == null || str.length() == 0) return result;

        HashSet<String> set = new HashSet<String>();
        char[] arr = str.toCharArray();
        int end = arr.length - 1;
        permutation(set, arr, 0, end);

        result.addAll(set); //将set集合中的元素放入list
        Collections.sort(result); //工具类自然排序
        return result;
    }

    /**
     * 求全排列
     * @param set 保存每次的排列
     * @param arr 字符数组
     * @param start 本次排列的开始位置
     * @param end 本次排列的结束位置
     */
    public void permutation(HashSet<String> set, char[] arr, int start, int end){
        if(start == end){  //排列到最后一个元素，set直接加入字符串（递归头）
            set.add(String.valueOf(arr)); //考虑字符重复
            return ;
        }

        for(int i = start; i <= end; i++){
            swap(arr, i, start); //第一个元素依次与后面的元素交换
            permutation(set, arr, start+1, end); //排列除了第一个元素之外的所有元素
            swap(arr, i, start); //恢复原始的排列顺序
        }
    }

    public void swap(char[] arr, int i, int j){
        char temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
