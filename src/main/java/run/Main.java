package run;

import graph.StateSpaceGraph;

public class Main {
    public static void main(String[] args) {
        StateSpaceGraph g = new StateSpaceGraph(args[0]);
        System.out.println(g.getNumVertices());
        System.out.println(g.getNumEdges());
    }
}
