package Algorithms.Graph.DirectedGraphs;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.ST;

/**
 *  SymbolDigraph 符号有向图
 */
public class SymbolDigraph {
    private ST<String, Integer> st;  //符号名-->索引
    private String[] keys;  //索引-->符号名
    private Digraph G;    //图

    /**
     * 构造函数
     * @param filename 文件名
     * @param sp 分隔符
     */
    public SymbolDigraph(String filename, String sp){
        st = new ST<String, Integer>();
        In in = new In(filename);
        while(in.hasNextLine()){
            String[] a = in.readLine().split(sp); //读取字符串
            for (int i = 0; i < a.length; i++) {
                if(!st.contains(a[i])){
                    st.put(a[i], st.size()); //每个不同的字符串关联一个索引
                }
            }
        }

        keys = new String[st.size()]; //用来获取顶点名的反向索引是一个数组
        for (String key : st.keys()) {
            keys[st.get(key)] = key;
        }

        G = new Digraph(st.size()); //构造有向图
        in = new In(filename);
        while(in.hasNextLine()){
            String[] a = in.readLine().split(sp); //邻接表
            int v = st.get(a[0]);
            for (int i = 1; i < a.length; i++) {
                G.addEdge(v, st.get(a[i]));
            }
        }
    }

    public boolean contains(String s){//key
        return st.contains(s);
    }

    public int index(String s){//key
        return st.get(s);
    }

    public String name(int v){
        return keys[v];
    }

    public Digraph G(){
        return G;
    }


}
