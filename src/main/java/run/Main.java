package run;

import graph.StateSpaceGraph;
import graph.exceptions.NodeNotFoundException;

public class Main {
    public static void main (String[] args) throws NodeNotFoundException {
        StateSpaceGraph ssg = new StateSpaceGraph(args[0]);

//        System.out.println(ssg);
//        System.out.println("---------------------------------------");
//        System.out.println(ssg.detailedEdges());

        System.out.println("max flow = " + ssg.dinic());
    }

}
