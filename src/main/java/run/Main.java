package run;

import graph.StateSpaceGraph;
import java.util.*;

public class Main {

    private static final String NODES = "------------------------- NODES -------------------------";
    private static final String EDGES = "------------------------- EDGES -------------------------";
    private static final String PATHS = "------------------------- PATHS -------------------------";
    private static final String GRAPH = "------------------------- GRAPH -------------------------";
    private static final String SPLIT = "---------------------------------------------------------";

    public static void main (String[] args) {
        StateSpaceGraph ssg = new StateSpaceGraph(args[0]);
        List<Deque<Integer>> paths = ssg.getPaths(10);

        //printGraph(ssg, paths);
        ssg.printStats(paths);

        for (Deque<Integer> path : paths)
            System.out.println(path);

    }



    /**
     * Prints graph data.
     *
     * @param ssg graph.
     */
    private static void printGraph(StateSpaceGraph ssg, List<Deque<Integer>> paths) {
        // nodes by id
        System.out.println(NODES);
        System.out.println(ssg.nodesToString());

        // edges by id
        System.out.println(EDGES);
        System.out.println(ssg.edgesToString());

        // incoming
        System.out.println(GRAPH);
        System.out.println(ssg.toString(true));

        // outgoing
        System.out.println(SPLIT);
        System.out.println(ssg.toString(false));

        // paths
        System.out.println(PATHS);
        System.out.println(ssg.pathsToString(paths));
    }

}
