package Algorithms.Graph.DirectedGraphs;

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

/**
 *  有向图深度优先搜索，解决单点可达性、多点可达性问题
 */
public class DirectedDFS {
    private boolean[] marked; //s到v,是否可达

    public DirectedDFS(Digraph G, int s){
        marked = new boolean[G.V()];
        validateVertex(s);
        dfs(G, s);
    }

    public DirectedDFS(Digraph G, Iterable<Integer> sources){
        marked = new boolean[G.V()];
        validateVertices(sources);
        for (int s : sources) {
            if(!marked[s]) dfs(G, s); //减少重复标记
        }
    }

    private void dfs(Digraph G, int v){
        marked[v] = true;

        for (int w : G.adj(v)) {
            if(!marked[w]) dfs(G, w);//减少重复标记
        }
    }

    public boolean marked(int v){
        validateVertex(v);
        return marked[v];
    }

    //验证单点有效性
    private void validateVertex(int v) {
        int V = marked.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    //验证多点有效性
    private void validateVertices(Iterable<Integer> vertices) {
        if (vertices == null) {
            throw new IllegalArgumentException("argument is null");
        }
        int V = marked.length;
        for (int v : vertices) {
            if (v < 0 || v >= V) {
                throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
            }
        }
    }

    public static void main(String[] args) {

        Digraph G = new Digraph(new In(args[0]));

        Bag<Integer> sources = new Bag<Integer>();
        for (int i = 1; i < args.length; i++) {
            sources.add(Integer.parseInt(args[i]));
        }

        DirectedDFS reachable = new DirectedDFS(G, sources);

        for (int v = 0; v < G.V(); v++) {
            if(reachable.marked(v)) StdOut.print(v + " ");
        }
        StdOut.println();
    }

}
