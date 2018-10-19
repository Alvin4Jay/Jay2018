package javabasiscode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * ArrayList LeetCode.LinkedList Vector 存放null值
 */
public class ListTest {

    public static void main(String[] args) {

        List list = new ArrayList();
        list.add(null);
        list.add(null);
        System.out.println(list.size());

        LinkedList linkedList = new LinkedList();
        linkedList.add(null);
        System.out.println(linkedList.size());

        Vector vector = new Vector();
        vector.add(null);
        System.out.println(vector.size());

    }

}
