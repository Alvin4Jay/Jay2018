package Algorithms.Graph.DirectedGraphs;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;

/**
 *  单点有向路径
 */
public class DepthFirstDirectedPaths {
    private boolean[] marked;  //s与任意顶点的连通性
    private int[] edgeTo; //所有从s连通的顶点构成的树，每个数组元素值代表从起点到达索引顶点的路径上的之前一个顶点
    private final int s;     //起点

    public DepthFirstDirectedPaths(Digraph G, int s){
        marked = new boolean[G.V()];
        edgeTo = new int[G.V()];
        validateVertex(s);
        this.s = s;
        dfs(G, s);
    }

    //搜索标记
    private void dfs(Digraph G, int v){
        marked[v] = true;
        for (int w : G.adj(v)) {
            if(!marked[w]){
                edgeTo[w] = v; //绳子，标记这条边v-->w
                dfs(G, w);
            }
        }
    }

    /**
     * 是否存在从s到v的路径
     * @param v
     * @return
     */
    public boolean hasPathTo(int v){
        validateVertex(v);
        return marked[v];
    }

    /**
     * s到v的路径，如果不存在，返回null
     * @param v
     * @return
     */
    public Iterable<Integer> pathTo(int v){
        validateVertex(v);
        if(!marked[v]) return null; //不连通

        Stack<Integer> path = new Stack<Integer>();
        for (int x = v; x != s; x = edgeTo[x]) { //退出条件
            path.push(x);
        }
        path.push(s);
        return path;
    }

    private void validateVertex(int v) {
        int V = marked.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in); //有向图
        // StdOut.println(G);

        int s = Integer.parseInt(args[1]);
        DepthFirstDirectedPaths dfs = new DepthFirstDirectedPaths(G, s); //起点s

        for (int v = 0; v < G.V(); v++) {
            if (dfs.hasPathTo(v)) { //s--->v ??
                StdOut.printf("%d to %d:  ", s, v);
                for (int x : dfs.pathTo(v)) {
                    if (x == s) StdOut.print(x);
                    else        StdOut.print("-" + x);
                }
                StdOut.println();
            }

            else {
                StdOut.printf("%d to %d:  not connected\n", s, v);
            }

        }
    }

}
