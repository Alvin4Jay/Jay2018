package 数据结构.Sword2Offer;

/**
 *  替换空格
 *  可以从后往前替换
 */
public class P4_ReplaceSpace {
    public static String replaceSpace(StringBuffer str) {
        String temp = str.toString();
        return temp.replaceAll("\\s","%20");  //正则表达式匹配

//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < str.length(); i++) {
//            if(str.charAt(i) == ' '){
//                sb.append('%').append('2').append('0');
//            }else{
//                sb.append(str.charAt(i));
//            }
//        }
//        return sb.toString();
    }

    public static void main(String[] args) {
        String str = " We Are Happy. ";
        StringBuffer sb = new StringBuffer(str);
        System.out.println(replaceSpace(sb));

    }
}
