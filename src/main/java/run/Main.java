package run;

import graph.StateSpaceGraph;

import java.util.*;

public class Main {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        StateSpaceGraph ssg = new StateSpaceGraph(args[0]);
        int numPaths  = 3;
        List<Deque<Integer>> paths = ssg.getPaths(numPaths);
        long finish = System.currentTimeMillis();
        float elapsed = (finish - start) / 1000.0f / 60.0f;
        ssg.printStats(paths, numPaths, elapsed);


        ssg.printGraph();
        //ssg.printPaths(paths);
    }
}
