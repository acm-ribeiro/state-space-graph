package run;

import graph.StateSpaceGraph;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class Main {

    private static final String STATS = "------------------------- STATS -------------------------";
    private static final String NODES = "------------------------- NODES -------------------------";
    private static final String EDGES = "------------------------- EDGES -------------------------";
    private static final String PATHS = "------------------------- PATHS -------------------------";
    private static final String GRAPH = "------------------------- GRAPH -------------------------";
    private static final String SPLIT = "---------------------------------------------------------";

    public static void main (String[] args) {
        StateSpaceGraph ssg = new StateSpaceGraph(args[0]);
        List<Deque<Integer>> paths = ssg.getPaths();

        printGraph(ssg, paths);
        printStats(ssg, paths);
    }

    /**
     * Computes the average path size.
     *
     * @param paths collection
     * @return average path size.
     */
    private static double averagePathSize(List<Deque<Integer>> paths) {
        long sum = 0L;

        for (Deque<Integer> path: paths)
            sum += path.size();

        return (double) sum / paths.size();
    }

    /**
     * Finds the largest path size in the given collection.
     *
     * @param paths collection
     * @return largest path size.
     */
    private static int largestPathSize(List<Deque<Integer>> paths) {
        int max = 0;

        for (Deque<Integer> path: paths)
            if(path.size() > max)
                max = path.size();

        return max;
    }

    /**
     * Finds the shortest path size in the given collection.
     *
     * @param paths collection
     * @return shortest path size.
     */
    private static int shortestPathSize(List<Deque<Integer>> paths) {
        int min = (int) averagePathSize(paths);

        for (Deque<Integer> path: paths)
            if(path.size() < min)
                min = path.size();

        return min;
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

    /**
     * Prints the number of nodes and edges in the graph. Also prints the number of complete paths,
     * the average path size, largest and shortest path sizes.
     *
     * @param ssg state space graph.
     * @param paths collection.
     */
    private static void printStats(StateSpaceGraph ssg, List<Deque<Integer>> paths) {
        System.out.println(STATS);
        System.out.printf("nodes      :   %d\n", ssg.getNumNodes());
        System.out.printf("edges      :   %d\n", ssg.getNumEdges());
        System.out.printf("paths      :   %d\n", paths.size());
        System.out.printf("avg size   :   %.3f\n", averagePathSize(paths));
        System.out.printf("max size   :   %d\n", largestPathSize(paths));
        System.out.printf("min size   :   %d\n", shortestPathSize(paths));
    }
}
