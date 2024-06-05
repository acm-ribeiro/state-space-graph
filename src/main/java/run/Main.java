package run;

import com.sun.jdi.IntegerValue;
import graph.StateSpaceGraph;

import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main (String[] args) {
        StateSpaceGraph ssg = new StateSpaceGraph(args[0]);

        System.out.println(ssg);
        System.out.println("---------------------------------------");

        System.out.println(ssg.detailedEdges());
        System.out.println("---------------------------------------");

        List<LinkedList<Integer>> paths = ssg.completePaths(ssg.mmBFS());
        for (LinkedList<Integer> p : paths) {
            for (Integer i : p)
                System.out.print(i + " ");
            System.out.println();
        }
        System.out.println("---------------------------------------");

        System.out.println(ssg.prevToString());

        ssg.completePaths(paths);
    }

}
