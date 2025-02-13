package run;

import graph.StateSpaceGraph;
import java.util.*;

public class Main {

    public static void main (String[] args) {
        StateSpaceGraph ssg = new StateSpaceGraph(args[0]);
        List<Deque<Integer>> paths = ssg.getPaths(2000);
        ssg.printStats(paths);
        //ssg.printGraph(paths);

        for (Deque<Integer> path : paths)
            System.out.println(Arrays.toString(ssg.getPathTransitions(path)));
    }
}
