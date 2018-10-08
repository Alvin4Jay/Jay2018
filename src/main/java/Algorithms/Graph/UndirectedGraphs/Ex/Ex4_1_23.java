package Algorithms.Graph.UndirectedGraphs.Ex;

import Algorithms.Graph.UndirectedGraphs.CC;
import Algorithms.Graph.UndirectedGraphs.Graph;
import Algorithms.Graph.UndirectedGraphs.SymbolGraph;

/**
 *
 */
public class Ex4_1_23 {
    public static void main(String[] args) {
        SymbolGraph sg = new SymbolGraph(args[0],args[1]);
        Graph g = sg.G();
        CC cc = new CC(g);

        int count = cc.count();
        System.out.println("图连通分量数: " + count);

        int sum = 0; //小于10
        for (int id = 0; id < count; id++) {
            int tempSum = 0;
            for (int j = 0; j < g.V(); j++) {
                if(id == cc.id(j)){
                    tempSum ++;
                }
            }
            if(tempSum < 10){
                sum ++;
            }
        }
        System.out.println("顶点数小于10的连通分量数: " + sum);
    }
}
