package Algorithms.Graph.UndirectedGraphs.Ex;

import Algorithms.Graph.UndirectedGraphs.Graph;
import edu.princeton.cs.algs4.In;

/**
 * 接受一幅图并打印
 */
public class Ex4_1_7 {
    public static void main(String[] args) {
        Graph graph = new Graph(new In(args[0]));

        System.out.println(graph.toString());

    }
}
