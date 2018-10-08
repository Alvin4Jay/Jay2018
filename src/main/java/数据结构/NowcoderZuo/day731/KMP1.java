package 数据结构.NowcoderZuo.day731;

public class KMP1 {
    public static int getIndexOf(String s, String m){
        if(s == null || m == null || m.length() < 1 || s.length() < m.length()) return -1;

        int si = 0;
        int mi = 0;
        char[] sc = s.toCharArray();
        char[] mc = m.toCharArray();
        int[] next = getNextArr(m);

        while(si < sc.length && mi < mc.length){
            if(sc[si] == mc[mi]){
                si ++;
                mi ++;
            }else if(mi > 0){
                mi = next[mi];
            }else {
                si ++;
            }
        }
        return mi == mc.length ? si -mi : -1;
    }

    public static int[] getNextArr(String m){
        if(m.length() == 1) return new int[]{-1};

        char[] chas = m.toCharArray();
        int[] next = new int[chas.length];
        next[0] = -1;
        next[1] = 0;
        int pos = 2;
        int cn = next[pos-1];

        while(pos < next.length){
            if(chas[pos-1] == chas[cn]){
                next[pos++] = ++cn;
            }else if(cn > 0){
                cn = next[cn];
            }else {
                next[pos++] = 0;
            }
        }
        return next;
    }

    public static void main(String[] args) {

        String s = "abcbcda";
        String m = "bcd";
        System.out.println(getIndexOf(s, m));
    }
}
