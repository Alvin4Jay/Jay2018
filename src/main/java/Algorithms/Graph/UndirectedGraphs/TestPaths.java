package Algorithms.Graph.UndirectedGraphs;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

/**
 *  寻找路径 测试
 */
public class TestPaths {
    public static void main(String[] args) {
        In in = new In(args[0]);
        Graph G = new Graph(in);
        int s = Integer.parseInt(args[1]);
        DepthFirstPaths dfs = new DepthFirstPaths(G, s);  //图，起点

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
