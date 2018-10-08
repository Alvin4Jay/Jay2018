package 数据结构.LeetCode.greedy;

import java.util.Arrays;

public class Ex455_Assign_Cookies {
    public int findContentChildren(int[] g, int[] s) {
        Arrays.sort(g);
        Arrays.sort(s);

        int pg = 0;
        int ps = 0;
        while(pg < g.length && ps < s.length){
            if(g[pg] <= s[ps]){
                pg ++;
                ps ++;
            }else {
                ps ++;
            }
        }
        return pg;
    }
}
