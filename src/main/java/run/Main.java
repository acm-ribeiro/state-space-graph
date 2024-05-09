package run;

import graph.StateSpaceGraph;
import graph.exceptions.NodeNotFoundException;

public class Main {
    public static void main (String[] args) throws NodeNotFoundException {
        StateSpaceGraph g = new StateSpaceGraph(args[0]);
//        System.out.print(g);
//        System.out.println(g.edgesToStringCompact());

        int maxFlow = g.dinic();

        System.out.println("max_f = " + maxFlow);
        System.out.println("nodes = " + g.getNumNodes());
        System.out.println("edges = " +g.getNumEdges());
    }
}
