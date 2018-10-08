package Algorithms.Graph.DirectedGraphs;

import Algorithms.Graph.ShortestPaths.DirectedEdge;
import Algorithms.Graph.ShortestPaths.EdgeWeightedDigraph;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.Stack;

/**
 * 有向图中基于深度优先搜索的顶点排序
 */
public class DepthFirstOrder {
    private boolean[] marked;
    private Queue<Integer> pre; //前序
    private Queue<Integer> post; //后序
    private Stack<Integer> reversePost; //逆后序

    public DepthFirstOrder(Digraph G){
        marked = new boolean[G.V()];
        pre = new Queue<Integer>();
        post = new Queue<Integer>();
        reversePost = new Stack<Integer>();

        for (int v = 0; v < G.V(); v++) {
            if(!marked[v]) dfs(G, v);
        }
    }

    public DepthFirstOrder(EdgeWeightedDigraph G){
        marked = new boolean[G.V()];
        pre = new Queue<Integer>();
        post = new Queue<Integer>();
        reversePost = new Stack<Integer>();

        for (int v = 0; v < G.V(); v++) {
            if(!marked[v]) dfs(G, v);
        }
    }

    private void dfs(Digraph G, int v){
        marked[v] = true;
        pre.enqueue(v);
        for (int w : G.adj(v)) {
            if(!marked[w]) dfs(G, w);
        }
        post.enqueue(v);
        reversePost.push(v);
    }

    private void dfs(EdgeWeightedDigraph G, int v){
        marked[v] = true;
        pre.enqueue(v);
        for (DirectedEdge e : G.adj(v)) {
            int w = e.to();
            if(!marked[w]) dfs(G, w);
        }
        post.enqueue(v);
        reversePost.push(v);
    }

    public Iterable<Integer> pre(){ //前序
        return pre;
    }

    public Iterable<Integer> post(){ //后序
        return post;
    }

    public Iterable<Integer> reversePost(){ //逆后序
        return reversePost;
    }
}
