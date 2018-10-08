package javabasiscode; /**
 * Created by Jay on 2017/10/11
 */
public class KMP {
    public static int getIndexOf(String str, String m){
        if(str == null || m == null || str.length() == 0 || m.length() == 0
                || str.length() < m.length()){
            return -1;
        }

        char[] ss = str.toCharArray();
        char[] ms = m.toCharArray();
        int si = 0;
        int mi = 0;
        int[] next = nextArr(ms);
        while(si < ss.length && mi < ms.length){
            if(ss[si] == ms[mi]){
                si++;
                mi++;
            }else if(mi > 0){
                mi = next[mi];
            }else {
                si++;
            }
        }
        return mi == ms.length ? si-mi : -1 ;
    }


    public static int[] nextArr(char[] ms){
        if(ms.length == 1) return new int[]{-1};

        int[] next = new int[ms.length];
        next[0] = -1;
        next[1] = 0;
        int pos = 2;
        int cn = next[1];

        while(pos < next.length){
            if(ms[pos-1] == ms[cn]){
                next[pos++] = ++cn;
            }else if(cn == 0){
                next[pos++] = 0;
            }else {
                cn = next[cn];
            }
        }
        return next;
    }

    public static void main(String[] args) {
        System.out.println(getIndexOf("abcdde", "def"));
    }

}
