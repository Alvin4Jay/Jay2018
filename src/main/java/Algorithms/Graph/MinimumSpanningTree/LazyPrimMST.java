package Algorithms.Graph.MinimumSpanningTree;

import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.Queue;

/**
 * 计算加权连通图的最小生成树的Prim算法的延时实现
 *
 * 1.MST 加入边
 * 2.MST 加入顶点
 * 3.更新 横切边的集合
 */
public class LazyPrimMST {
    private boolean[] marked; //MST的顶点
    private Queue<Edge> mst;  //MST的边
    private MinPQ<Edge> pq;  //横切边的优先队列 (包含无效的边)

    private double weight; //mst权重

    public LazyPrimMST(EdgeWeightedGraph G){
        marked = new boolean[G.V()];
        mst = new Queue<Edge>();
        pq = new MinPQ<Edge>();

        visit(G, 0); //初始化

        while(!pq.isEmpty()){
            Edge e = pq.delMin(); //影响效率

            int v = e.either(); int w = e.other(v);
            if(marked[v]&&marked[w]) continue;  //跳过无效的边

            mst.enqueue(e); //有效边加入MST
            weight += e.weight();  //计算mst权重
            if(!marked[v]) visit(G, v);  //将顶点加入MST，更新pq
            if(!marked[w]) visit(G, w);
        }
    }

    private void visit(EdgeWeightedGraph G, int v){
        marked[v] = true;  //顶点v加入 MST
        for (Edge e : G.adj(v)) {
            if(!marked[e.other(v)]) pq.insert(e);  //跟v相连的边如果没失效，加入到pq
        }
    }

    public Iterable<Edge> edges(){
        return mst;
    }

    public double weight(){
        return weight;
    }

}
