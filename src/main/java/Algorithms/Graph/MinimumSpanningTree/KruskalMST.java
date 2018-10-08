package Algorithms.Graph.MinimumSpanningTree;

import edu.princeton.cs.algs4.MinPQ;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.UF;

/**
 * 加权连通图的最小生成树计算 Kruskal算法
 */
public class KruskalMST {
    private Queue<Edge> mst; //最小生成树的边
    private double weight; //总权重

    public KruskalMST(EdgeWeightedGraph G){
        mst = new Queue<Edge>();  //初始化
        MinPQ<Edge> pq = new MinPQ<Edge>();
        for (Edge e : G.edges()) { //按照顺序排列的所有边
            pq.insert(e);
        }
        UF uf = new UF(G.V()); //检查是否构成环

        while(!pq.isEmpty() && mst.size() < G.V()-1){
            Edge e = pq.delMin();  //pq中权重最小的边和它的顶点
            int v = e.either();int w= e.other(v);
            if(uf.connected(v, w)) continue; //v,w已经在一个连通分量上，忽略失效的边，即构成环的边

            uf.connected(v, w);  //将两个分量连接为一个分量
            mst.enqueue(e);  //将边加入到最小生成树中
            weight += e.weight(); //更新权重
        }
    }

    public Iterable<Edge> edges(){
        return mst;
    }

    public double weight(){
        return weight;
    }
}
