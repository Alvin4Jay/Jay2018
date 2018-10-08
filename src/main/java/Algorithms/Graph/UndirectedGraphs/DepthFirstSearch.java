package Algorithms.Graph.UndirectedGraphs;

/**
 * 深度优先搜索 DFSWithPath
 * 注意：算法遍历边和访问顶点的顺序与图的表示(邻接表)有关，而不只是与图的结构或是算法有关
 */
public class DepthFirstSearch implements Search{
    private boolean[] marked;  //是否连通的标记，s-v
    private int count; //与s连通的顶点总数

    public DepthFirstSearch(Graph G, int s) {
        marked = new boolean[G.V()];
        validateVertex(s); //验证s
        dfs(G, s);  //找到和s连通的所有顶点
    }

    private void dfs(Graph G, int v){
        marked[v] = true;  //连通
        count ++;  //总数+1
        for (int w : G.adj(v)) { //v的所有相邻顶点
            if(!marked[w]) dfs(G, w);
        }
    }

    @Override
    public boolean marked(int w) {
        validateVertex(w); //验证w
        return marked[w];
    }

    @Override
    public int count() {
        return count;
    }

    //验证顶点数值是否有效
    private void validateVertex(int v) {
        int V = marked.length;
        if (v < 0 || v >= V)
            throw new IllegalArgumentException("vertex " + v + " is not between 0 and " + (V-1));
    }

}
