package Algorithms.Graph.UndirectedGraphs;

/**
 * 连通分量（统计连通子图数量） CC
 */
public class CC {
    private boolean[] marked; //标记过了吗
    private int[] id; //顶点所在连通分量的标识符（0到count()-1）
    private int count; //连通分量总个数

    public CC(Graph G){
        marked = new boolean[G.V()];
        id = new int[G.V()];

        for (int s = 0; s < G.V(); s++) {
            if(!marked[s]) {
                dfs(G, s);
                count++; //每查找到一个连通分量，count+1
            }
        }
    }
    //一次深度优先搜索，查找一个联通分量
    private void dfs(Graph G, int v){
        marked[v] = true;
        id[v] = count;
        for (int w : G.adj(v)) {
            if(!marked[w]){
                dfs(G, w);
            }
        }
    }

    public boolean connected(int v, int w){
        return id[v] == id[w];
    }

    public int count(){
        return count;
    }

    public int id(int v){
        return id[v];
    }
}
