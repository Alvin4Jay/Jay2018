package 数据结构.NowcoderZuo.day0807;

/**
 * 判断t1树中是否有和t2树拓扑结构完全相同的子树（利用树的序列化）
 */
public class Problem_06_IsSubtree {
    public boolean isSubtree(Node t1, Node t2){
        String s1 = serialByPre(t1);
        String s2 = serialByPre(t2);

        return false;
    }

    //树的序列化(先序遍历)
    public String serialByPre(Node head){
        if(head == null) {
            return "#!";
        }
        String res = head.value + "!";
        res += serialByPre(head.left);
        res += serialByPre(head.right);
        return res;
    }

    //KMP
    public int getIndexOf(String s, String m){
        if(s == null || m == null || s.length() < m.length()){
            return -1;
        }
        char[] sc = s.toCharArray();
        char[] mc = m.toCharArray();
        int[] next = getNextArray(mc);
        int si = 0;
        int mi = 0;

        while(si < sc.length && mi < mc.length){
            if(sc[si] == mc[mi]){
                si++;
                mi++;
            }else if(next[mi] != -1){
                mi = next[mi];
            }else {
                si ++;
            }
        }
        return mi == mc.length ?  si-mi: -1;
    }

    public int[] getNextArray(char[] mc){
        if(mc.length == 1){
            return new int[]{-1};
        }
        int[] next = new int[mc.length];
        next[0] = -1;
        next[1] = 0;
        int cn = 0;
        int pos = 2;

        while(pos < next.length){
            if(mc[pos-1] == mc[cn]){
                next[pos++] = ++cn;
            }else if(cn > 0){
                cn = next[cn];
            }else {
                next[pos++] = 0;
            }
        }
        return next;
    }

    public class Node{
        public int value;
        public Node left;
        public Node right;

        public Node(int value) {
            this.value = value;
        }
    }
}
