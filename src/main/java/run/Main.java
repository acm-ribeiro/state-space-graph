package run;

import graph.StateSpaceGraph;

import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main (String[] args) {
        StateSpaceGraph ssg = new StateSpaceGraph(args[0]);

        List<List<String>> nominal = ssg.getPaths();
        for (List<String> n : nominal)
            System.out.println(n);
    }

}
