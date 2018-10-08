package Algorithms.Graph.DirectedGraphs.Ex;

import Algorithms.Graph.DirectedGraphs.DepthFirstOrder;
import Algorithms.Graph.DirectedGraphs.Digraph;
import edu.princeton.cs.algs4.In;

/**
 *
 */
public class Ex4_2_17 {
    public static void main(String[] args) {

        Digraph G = new Digraph(new In(args[0]));

        DepthFirstOrder order = new DepthFirstOrder(G.reverse());
        //1
        Iterable<Integer> GRReversePost = order.reversePost();
        System.out.println(GRReversePost);

        //2
        DepthFirstOrder order2 = new DepthFirstOrder(G);
        Iterable<Integer> post = order2.post();
        System.out.println(post);
    }
}
