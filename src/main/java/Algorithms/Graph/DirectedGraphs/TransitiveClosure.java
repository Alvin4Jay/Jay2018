package Algorithms.Graph.DirectedGraphs;

/**
 * 传递闭包————顶点对的可达性 s--->w
 */
public class TransitiveClosure {
    private DirectedDFS[] all;

    public TransitiveClosure(Digraph G){
        all = new DirectedDFS[G.V()];
        for (int v = 0; v < G.V(); v++) {
            all[v] = new DirectedDFS(G, v); //计算从v开始的可达性
        }
    }

    public boolean reachable(int v, int w){
        return all[v].marked(w);
    }

}
