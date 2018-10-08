package Algorithms.Graph.UndirectedGraphs;

import edu.princeton.cs.algs4.Stack;

/**
 *  利用深度优先搜索查找路径（从起点s到达任意连通顶点的路径）
 */
public class DepthFirstPaths {
    private boolean[] marked;  //s与任意顶点的连通性
    private int[] edgeTo; //所有从s连通的顶点构成的树，每个数组元素值代表从起点到达索引顶点的路径上的之前一个顶点
    private final int s;     //起点

    public DepthFirstPaths(Graph G, int s){
        marked = new boolean[G.V()];
        edgeTo = new int[G.V()];
        validateVertex(s);
        this.s = s;
        dfs(G, s);
    }

    //搜索标记
    private void dfs(Graph G, int v){
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

}
