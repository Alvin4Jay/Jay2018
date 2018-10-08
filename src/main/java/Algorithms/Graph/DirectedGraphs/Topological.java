package Algorithms.Graph.DirectedGraphs;

import Algorithms.Graph.ShortestPaths.EdgeWeightedDigraph;
import Algorithms.Graph.ShortestPaths.EdgeWeightedDirectedCycle ;
import edu.princeton.cs.algs4.StdOut;

/**
 *  有向无环图————拓扑排序
 */
public class Topological {
    private Iterable<Integer> order; //顶点的拓扑排序
    private int[] rank;  //顶点拓扑排序中的顶点位置

    public Topological(Digraph G){
        DirectedCycle cycleFinder = new DirectedCycle(G); //先检查是否存在有向环

        if(!cycleFinder.hasCycle()){ //是有向无环图
            DepthFirstOrder dfs = new DepthFirstOrder(G);//再得到逆后序
            order = dfs.reversePost();
            rank = new int[G.V()];
            int i = 0;
            for (int v : order)
                rank[v] = i++;
        }
    }

    public Topological(EdgeWeightedDigraph G){
        EdgeWeightedDirectedCycle finder = new EdgeWeightedDirectedCycle(G);
        if(!finder.hasCycle()){
            DepthFirstOrder dfs = new DepthFirstOrder(G);
            order = dfs.reversePost();
        }
    }

    /**
     * 返回拓扑排序顺序
     * @return
     */
    public Iterable<Integer> order(){
        return order;
    }

    public boolean hasOrder(){
        return order != null;
    }

    /**
     * 是否是有向无环图
     * @return
     */
    public boolean isDAG(){
        return order != null;
    }

    /**
     * 拓扑排序中顶点的位置
     * @param v
     * @return
     */
    public int rank(int v) {
        validateVertex(v);
        if (hasOrder()) return rank[v];
        else            return -1;
    }

    // throw an IllegalArgumentException unless {@code 0 <= v < V}
    private void validateVertex(int v) {
        int V = rank.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

    public static void main(String[] args) {
        String filename = args[0]; //文件名
        String separator = args[1]; //分隔符
        SymbolDigraph sg = new SymbolDigraph(filename, separator); //符号有向图

        Topological top = new Topological(sg.G()); //拓扑排序

        for (int v : top.order) {
            StdOut.println(sg.name(v)); //处数拓扑排序(栈),优先级从高到低
        }
    }
}
