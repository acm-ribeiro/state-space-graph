package run;

import graph.Edge;
import graph.StateSpaceGraph;

import java.util.List;

public class Main {
    public static void main (String[] args) {
        StateSpaceGraph ssg = new StateSpaceGraph(args[0]);

        List<List<Edge>> nominal = ssg.getPaths();
        for (List<Edge> n : nominal) {
            for (Edge e : n)
                System.out.print(e.getLabel() + " ");
            System.out.println();
        }
    }

}
