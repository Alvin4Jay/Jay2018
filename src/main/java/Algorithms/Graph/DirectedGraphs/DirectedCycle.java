package Algorithms.Graph.DirectedGraphs;

import edu.princeton.cs.algs4.Stack;

/**
 * 有向图有向环的检测
 */
public class DirectedCycle {
    private boolean[] marked; //dfs过了吗，可达吗，标记过了吗
    private int[] edgeTo; //保存路径
    private  boolean[] onStack;  //递归调用栈上的顶点，未出栈
    private Stack<Integer> cycle; //如果存在有向环，表示有向环上的所有顶点。

    public DirectedCycle(Digraph G){
        this.marked = new boolean[G.V()];
        this.edgeTo = new int[G.V()];
        this.onStack = new boolean[G.V()];

        for (int v = 0; v < G.V(); v++) {
            if(!marked[v]) dfs(G, v); //遍历
        }
    }


    private void dfs(Digraph G, int v){
        marked[v] = true;
        onStack[v] = true; //入栈
        for (int w : G.adj(v)) {
            if(hasCycle()) return; //如果有向环已存在，则直接返回
            else if(!marked[w]) { //w未标记且不再调用栈上，dfs
                edgeTo[w] = v;
                dfs(G, w);
            }else if(onStack[w]){ //w已经标记过且在调用栈上
                cycle = new Stack<Integer>();
                for (int x = v; x != w ; x = edgeTo[x]) { //从v原路返回到w，输出路径
                    cycle.push(x);
                }
                cycle.push(w);
                cycle.push(v);
            }
        }
        onStack[v] = false; //出栈
    }

    public boolean hasCycle(){
        return cycle != null;
    }

    public Iterable<Integer> cycle(){
        return cycle;
    }

}
