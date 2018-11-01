package javabasiscode;

import java.util.LinkedList;
import java.util.Stack;

/**
 * Created by Jay on 2017/9/15
 */
public class DFSWithPath {
    private boolean[] marked;
    private int[] edgeTo;
    private final int s;


    public DFSWithPath(Graph G, int s){
        marked = new boolean[G.V()];
        edgeTo = new int[G.V()];
        this.s = s;
        dfs(G, s);
    }

    private void dfs(Graph G, int v){
        marked[v] = true;

        for (int w : G.adj(v)) {
            if(!marked[w]) {
                edgeTo[w] = v;
                dfs(G, w);
            }
        }
    }

    public boolean marked(int w){
        return marked[w];
    }

    public boolean hasPathTo(int v){
        return marked[v];
    }

    public Iterable<Integer> pathTo(int v){
        if(!hasPathTo(v)) return null;
        Stack<Integer> path = new Stack<Integer>();
        for(int x = v; x != s; x = edgeTo[x]){
            path.push(x);
        }
        path.push(s);
        return path;
    }

}

class Graph{
    private final int V;
    private int E;
    private LinkedList<Integer>[] adj; //邻接表

    public Graph(int V){
        this.V = V;
        this.E = 0;
        adj = (LinkedList<Integer>[]) new LinkedList[V];
        for (int i = 0; i < adj.length; i++) {
            adj[i] = new LinkedList<Integer>();
        }
    }

    public int V(){return V;}
    public int E(){return E;}

    public void addEdge(int v, int w){
        adj[v].addFirst(w);
        adj[w].addFirst(v);
        E ++;
    }

    public Iterable<Integer> adj(int v){
        return adj[v];
    }

}
