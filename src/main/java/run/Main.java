package run;

import graph.Edge;
import graph.StateSpaceGraph;
import java.util.*;

public class Main {

    public static void main (String[] args) {
        StateSpaceGraph ssg = new StateSpaceGraph(args[0]);
        List<Deque<Integer>> paths = ssg.getPaths(2000);
        //ssg.printStats(paths);
        //ssg.printGraph();

        Edge[] path;
        for (Deque<Integer> p : paths) {
            System.out.println("[" + p.size() + "]" + p);
            path = ssg.getPathEdges(p);
            for (Edge e : path)
                System.out.println(e.getTransition() + Arrays.toString(e.getParameters()));
        }
    }
}
