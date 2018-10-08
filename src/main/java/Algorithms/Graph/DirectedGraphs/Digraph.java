package Algorithms.Graph.DirectedGraphs;

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Stack;

/**
 * 有向图
 */
public class Digraph {
    private final int V;  //顶点数
    private int E;  //边数
    private Bag<Integer>[] adj; //邻接表,头插法
    private int[] indegree;  //各顶点的入度

    public Digraph(int V) {
        if (V < 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");

        this.V = V;this.E = 0;
        indegree = new int[this.V];
        adj= (Bag<Integer>[]) new Bag[V];
        for (int v = 0; v < V; v++) {   //初始化
            adj[v] = new Bag<Integer>();
        }
    }

    //从标准输入流in读入一幅图
    public Digraph(In in){
        this(in.readInt());
        int E = in.readInt();
        if (E < 0) throw new IllegalArgumentException("number of edges in a Graph must be nonnegative");
        for (int i = 0; i < E; i++) {  //添加边
            if(in.hasNextLine()){
                int v = in.readInt();
                int w = in.readInt();
                validateVertex(v);  //对输入的顶点做验证
                validateVertex(w);
                addEdge(v, w);
            }
        }
    }

    /**
     * G的复制，副本
     * @param G 有向图
     */
    public Digraph(Digraph G){
        this(G.V());  //初始化及顶点数
        this.E = G.E(); //边数
        for (int v = 0; v < this.V; v++) {
            this.indegree[v] = G.indegree(v); //入度
        }
        for (int v = 0; v < V; v++) { //添加邻接表
            Stack<Integer> temp = new Stack<Integer>();
            for (int w : G.adj(v)) {
                temp.push(w);
            }
            for (int w : temp) { //顺序要一致
                adj[v].add(w);
            }
        }
    }

    public void addEdge(int v, int w){
        validateVertex(v);  //验证顶点数值
        validateVertex(w);
        adj[v].add(w);  //有向图这里调用一次，v--->w
        indegree[w] ++; //w入度+1
        E ++;
    }

    public int E(){
        return E;
    }

    public int V(){
        return V;
    }

    public Iterable<Integer> adj(int v){
        validateVertex(v);  //验证顶点数值
        return adj[v];
    }

    public int outdegree(int v){
        validateVertex(v);
        return adj[v].size();
    }

    public int indegree(int v){
        validateVertex(v);
        return indegree[v];
    }

    //有向图的反向图
    public Digraph reverse(){
        Digraph R = new Digraph(this.V);
        for (int v = 0; v < V; v++) {
            for (int w : adj(v)) {
                R.addEdge(w, v); //反向添加边，头插法
            }
        }
        return R;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    //toString()方法
    public String toString(){
        StringBuilder s = new StringBuilder(V + " vertices, " + E + " edges\r\n");
        for (int v = 0; v < V; v++) {
            s.append(v).append(": ");
            for(int w : this.adj(v)){
                s.append(w).append(" ");
            }
            s.append("\r\n");
        }
        return s.toString();
    }

}
