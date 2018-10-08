package Algorithms.Graph.UndirectedGraphs;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

/**
 *  无向图处理用例
 */
public class TestSearch {
    public static void main(String[] args) {
        In in = new In(args[0]);
        Graph G = new Graph(in);
        int s = Integer.parseInt(args[1]);
        Search search = new DepthFirstSearch(G, s);
        for (int v = 0; v < G.V(); v++) {
            if (search.marked(v))   //包含自己，s与v是否连通
                StdOut.print(v + " ");
        }

        StdOut.println();
        if (search.count() != G.V()) StdOut.println("NOT connected");
        //与s连通的顶点个数等于总顶点个数，表示图是连通的
        else                         StdOut.println("connected");
    }
}
