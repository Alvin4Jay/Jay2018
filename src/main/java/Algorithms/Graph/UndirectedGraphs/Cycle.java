package Algorithms.Graph.UndirectedGraphs;

/**
 *  是否为无环图判断，假设不存在自环或者平行边
 */
public class Cycle {
    private boolean[] marked;  //标记过了吗
    private boolean hasCycle; //是否有环

    public Cycle(Graph G){
        marked = new boolean[G.V()];
        for (int s = 0; s < G.V(); s++) {
            if(!marked[s]){
                dfs(G, s, s);
            }
        }
    }

    /**
     * dfs
     * @param G 图
     * @param v 现在的点
     * @param u 前面的点  u-->v
     */
    private void dfs(Graph G, int v, int u){
        marked[v] = true;
        for (int w : G.adj(v)) {
            if(!marked[w]){
                dfs(G, w, v);
            }
            else if(w != u) hasCycle = true; //v的相邻点不是u，则是环
        }
    }

    public boolean hasCycle(){
        return hasCycle;
    }

}
