package Algorithms.Graph.ShortestPaths;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

/**
 * 最短路径树
 */
public class SP {
    public SP(EdgeWeightedDigraph G, int s){

    }

    double distTo(int v){
        return 0;
    }

    boolean hasPathTo(int v){
        return false;
    }

    Iterable<DirectedEdge> pathTo(int v){
        return null;
    }

    public static void main(String[] args) {
        EdgeWeightedDigraph G = new EdgeWeightedDigraph(new In(args[0]));
        int s = Integer.parseInt(args[1]);
        SP sp = new SP(G, s);

        for (int t = 0; t < G.V(); t++) {
            StdOut.print(s + " to " + t);
            StdOut.printf(" (%4.2f): ", sp.distTo(t));
            if(sp.hasPathTo(t)){
                for (DirectedEdge e : sp.pathTo(t)) {
                    StdOut.printf(e + " ");
                }
            }
            StdOut.println();
        }
    }
}
