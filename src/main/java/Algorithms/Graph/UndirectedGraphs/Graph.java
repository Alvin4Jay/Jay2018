package Algorithms.Graph.UndirectedGraphs;

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Stack;

/**
 *  无向图(邻接表结构)
 */
public class Graph {
    private final int V;  //顶点数
    private int E;  //边数
    private Bag<Integer>[] adj; //邻接表,头插法

    //创建一个含有V个顶点但不含有任何边的图
    public Graph(int V){
        if (V < 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");

        this.V = V;this.E = 0;
        adj= (Bag<Integer>[]) new Bag[V];
        for (int v = 0; v < V; v++) {   //初始化
            adj[v] = new Bag<Integer>();
        }
    }

    //从标准输入流in读入一幅图
    public Graph(In in){
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

    //生成G的副本
    public Graph(Graph G){
        this(G.V());
        this.E = G.E();
        //与原图保持邻接表顺序的一致性
        for (int v = 0; v < V; v++) {
            Stack<Integer> s = new Stack<Integer>();
            for (int w : G.adj(v)) {
                s.push(w);
            }
            for (int n : s) {
                adj[v].add(n);
            }
        }
    }

    public void addEdge(int v, int w){
        validateVertex(v);  //验证顶点数值
        validateVertex(w);
        adj[v].add(w);
        adj[w].add(v);
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

    //计算顶点的度
    public int degree(int v){
        validateVertex(v);
        return adj[v].size();
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
