package Algorithms.Graph.MinimumSpanningTree;

import edu.princeton.cs.algs4.IndexMinPQ;
import edu.princeton.cs.algs4.Queue;

/**
 * 计算加权连通图的最小生成树的Prim算法  即时实现
 * 1.找出下一个被标记的顶点，加入树中
 * 2.更新edgeTo、distTo数组
 * 3.更新索引优先队列pq
 */
public class PrimMST {
    private boolean[] marked; //如果顶点v在树中，标记为true
    private Edge[] edgeTo;  //距离树的最短边
    private double[] distTo; //距离树的最短边的权值
    private IndexMinPQ<Double> pq; //有效的横切边 不在树中的顶点距离树最近的权值

    public PrimMST(EdgeWeightedGraph G){
        marked = new boolean[G.V()];
        edgeTo = new Edge[G.V()];
        distTo = new double[G.V()];
        for (int v = 0; v < G.V(); v++) {
            distTo[v] = Double.POSITIVE_INFINITY; //初始化为正无穷
        }
        pq = new IndexMinPQ<Double>(G.V()); //索引从0到V-1 （索引，权值）

        //初始化
        edgeTo[0] = null;
        distTo[0] = 0.0;
        pq.insert(0, 0.0);

        while(!pq.isEmpty()){
            visit(G, pq.delMin()); //比较权值，返回索引，返回距离最近的顶点，加入树中
        }
    }

    private void visit(EdgeWeightedGraph G, int v){
        marked[v]  = true; //加入MST树，更新数据

        for (Edge e : G.adj(v)) {
            int w = e.other(v);
            if(marked[w]) continue; //w已经在树中， v-w失效
            if(e.weight() < distTo[w]){
                edgeTo[w] = e;  //修改w到MST的最短边为e
                distTo[w] = e.weight(); //修改最短边的权值

                if(pq.contains(w)) pq.changeKey(w, distTo[w]); //修改pq中w的权值
                else pq.insert(w, distTo[w]);  //如果pq中没有顶点w，插入
            }
        }
    }

    public Iterable<Edge> edges(){
        Queue<Edge> mst = new Queue<Edge>();
        for (Edge e : edgeTo) {
            if (e != null) { //顶点0对应的边为null
                mst.enqueue(e);
            }
        }
        return mst;
    }

    public double weight(){
        double weight = 0.0;
        for (double w : distTo) {
            weight += w;
        }
        return weight;
    }
}
