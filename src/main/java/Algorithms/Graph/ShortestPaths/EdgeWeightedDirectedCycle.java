package Algorithms.Graph.ShortestPaths;

import edu.princeton.cs.algs4.Stack;

/**
 * 检查加权有向图是否有 有向环
 */
public class EdgeWeightedDirectedCycle {
    private boolean[] marked; //dfs过了吗，可达吗，标记过了吗
    private DirectedEdge[] edgeTo; //保存路径
    private  boolean[] onStack;  //递归调用栈上的顶点，未出栈
    private Stack<DirectedEdge> cycle; //如果存在有向环，表示有向环上的所有顶点。

    public EdgeWeightedDirectedCycle(EdgeWeightedDigraph G){
        this.marked = new boolean[G.V()];
        this.edgeTo = new DirectedEdge[G.V()];
        this.onStack = new boolean[G.V()];

        for (int v = 0; v < G.V(); v++) {
            if(!marked[v]) dfs(G, v); //遍历
        }
    }


    private void dfs(EdgeWeightedDigraph G, int v){
        marked[v] = true;
        onStack[v] = true; //入栈
        for (DirectedEdge e : G.adj(v)) {
            int w = e.to();
            if(hasCycle()) return; //如果有向环已存在，则直接返回
            else if(!marked[w]) { //w未标记且不再调用栈上，dfs
                edgeTo[w] = e;
                dfs(G, w);
            }else if(onStack[w]){ //w已经标记过且在调用栈上
                cycle = new Stack<DirectedEdge>();
                DirectedEdge f = e;
                while (f.from() != w) {
                    cycle.push(f);
                    f = edgeTo[f.from()];
                }
                cycle.push(f);
            }
        }
        onStack[v] = false; //出栈
    }

    public boolean hasCycle(){
        return cycle != null;
    }

    public Iterable<DirectedEdge> cycle(){
        return cycle;
    }
}
