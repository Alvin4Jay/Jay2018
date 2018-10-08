package Algorithms.Graph.DirectedGraphs;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;
import edu.princeton.cs.algs4.StdOut;

/**
 * 单点最短有向路径
 */
public class BreadthFirstDirectedPaths {
    private boolean[] marked;  //到达该顶点的最短路径已知吗？或者是否连通，最短路径已知
    private int[] edgeTo;   //到达该顶点最短路径上的最后一个顶点
    private final int s;   //起点
    private static final int INFINITY = Integer.MAX_VALUE;
    private int[] distTo; //s到v的最短路径边数

    public BreadthFirstDirectedPaths(Digraph G, int s){
        marked = new boolean[G.V()];
        edgeTo = new int[G.V()];
        distTo = new int[G.V()];
        for (int i = 0; i < distTo.length; i++) {
            distTo[i] = INFINITY;
        }
        validateVertex(s);
        this.s = s;
        bfs(G, s);
    }

    private void bfs(Digraph G, int s){
        Queue<Integer> queue = new Queue<Integer>();
        marked[s] = true;   //标记起点
        distTo[s] = 0; //起点边数为0

        queue.enqueue(s);  //起点入队列

        while(!queue.isEmpty()){
            int v = queue.dequeue();  //出队列
            for (int w : G.adj(v)) {
                if(!marked[w]){    //如果相邻的顶点未被标记
                    edgeTo[w] = v;   //保存最短路径的最后一条边
                    marked[w] = true; //标记连通
                    distTo[w] = distTo[v] + 1; //到达w的边数为到达v的边数+1
                    queue.enqueue(w); //入队列
                }
            }
        }
    }

    public boolean hasPathTo(int v){
        validateVertex(v);
        return marked[v];
    }

    public Iterable<Integer> pathTo(int v){
        validateVertex(v);
        if(!marked[v]) return null; //不连通

        Stack<Integer> path = new Stack<Integer>();
        for (int x = v; x != s; x = edgeTo[x]) {
            path.push(x);
        }
        path.push(s);
        return path;
    }

    //边数s---v
    public int distTo(int v){
        validateVertex(v);

        return distTo[v];
    }

    private void validateVertex(int v) {
        int V = marked.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }


    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        int s = Integer.parseInt(args[1]);
        BreadthFirstDirectedPaths dfs = new BreadthFirstDirectedPaths(G, s);  //图，起点

        for (int v = 0; v < G.V(); v++) {
            if (dfs.hasPathTo(v)) {  //s与v连通
                StdOut.printf("%d to %d:  ", s, v);
                for (int x : dfs.pathTo(v)) {   //stack
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
