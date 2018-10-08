package Wc_Skill.IdeaHotKeys.practiceFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jay on 2018/1/24
 */
public class Main {
    public static void main(String[] args){
        List<String> list  = new ArrayList<String>();
        list.add("zhangsan");
        list.add("lisi");
        list.add("wangwu");

        for (String s : list) {
            System.out.println(s);
        }

        String result = getResult(list);//3
        System.out.println(result);

        for (String aList : list) {
            System.out.println(aList);
        }


    }

    private static String getResult(List<String> list){
        if(list == null || list.size() == 0){
            return null;
        }
        StringBuilder sb = new StringBuilder("");
        for (String s : list) {
            sb.append(s).append(" ");
        }
        String result = sb.toString();

        return result.substring(0, result.length()-1);
    }

}
