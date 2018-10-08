package Algorithms.Graph.UndirectedGraphs;

/**
 *  Search api
 */
public interface Search {
    /**
     * w与s是否连通
     * @param w 顶点
     * @return 返回连通与否
     */
    boolean marked(int w);

    /**
     * 与s连通的顶点个数
     * @return 个数
     */
    int count();
}
