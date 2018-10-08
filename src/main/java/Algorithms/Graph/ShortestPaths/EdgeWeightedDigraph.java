package Algorithms.Graph.ShortestPaths;

import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.In;

/**
 *  加权有向图
 */
public class EdgeWeightedDigraph {
    private final int V;   //顶点个数
    private int E;  //边的总数
    private Bag<DirectedEdge>[] adj; //邻接表

    public EdgeWeightedDigraph(int V){
        if (V < 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");
        this.V = V;
        this.E = 0;
        adj = (Bag<DirectedEdge>[])new Bag[V];
        for (int v = 0; v < V; v++) {
            adj[v] = new Bag<DirectedEdge>();
        }
    }

    public EdgeWeightedDigraph(In in){
        this(in.readInt());
        int E = in.readInt();
        if (E < 0) throw new IllegalArgumentException("Number of edges must be nonnegative");
        for (int i = 0; i < E; i++) {
            int v = in.readInt();
            int w = in.readInt();
            validateVertex(v);
            validateVertex(w);
            double weight = in.readDouble();
            DirectedEdge e = new DirectedEdge(v, w, weight);
            addEdge(e);
        }
    }

    public void addEdge(DirectedEdge e){
        adj[e.from()].add(e);
        E ++;
    }

    public int V(){
        return V;
    }

    public int E(){
        return E;
    }

    public Iterable<DirectedEdge> adj(int v){
        validateVertex(v);
        return adj[v];
    }

    public Iterable<DirectedEdge> edges(){
        Bag<DirectedEdge> bag = new Bag<DirectedEdge>();
        for (int v = 0; v < V; v++) {
            for (DirectedEdge e : adj[v]) {
                bag.add(e);
            }
        }
        return bag;
    }

    private void validateVertex(int v) {
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(V).append(" ").append(E).append("\r\n");
        for (int v = 0; v < V; v++) {
            s.append(v + ": ");
            for (DirectedEdge e : adj[v]) {
                s.append(e + "  ");
            }
            s.append("\r\n");
        }
        return s.toString();
    }
}
