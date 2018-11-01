package javabasiscode;

import java.util.ArrayList;
import java.util.List;

/**
 *  列表迭代测试
 */
public class ListIteratorTest {

    public static void main(String[] args) {
        LouShanTest();
        BuWanQuanShanChu();

    }

    //List漏删测试
    public static void LouShanTest(){
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(2);
        list.add(3);
        list.add(4);
        System.out.println("----------list大小1：--"+list.size());
        for (int i = 0; i < list.size(); i++) {
            if (2 == list.get(i)) {
                list.remove(i); //删除2
            }
            System.out.println(list.get(i));
        }
        System.out.println("最后输出=" + list.toString()); //没有删除干净
    }

    //测试不完全删除
    public static void BuWanQuanShanChu(){
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(2);
        list.add(3);
        list.add(4);
        System.out.println("----------list大小1：--"+list.size());
        for (int i = 0; i < list.size(); i++) {
            list.remove(i);
        }
        System.out.println("最后输出=" + list.toString());
    }


}
