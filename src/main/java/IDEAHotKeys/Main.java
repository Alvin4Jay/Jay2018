package IDEAHotKeys;import java.util.ArrayList;
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

        String result = getResult(list);
        System.out.println(result);

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
