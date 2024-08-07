package run;

import graph.StateSpaceGraph;

import java.util.Deque;
import java.util.List;

public class Main {
    public static void main (String[] args) {
        StateSpaceGraph ssg = new StateSpaceGraph(args[0]);

        System.out.println("------------------------------- GRAPH -------------------------------");
        System.out.println(ssg.toString(true));  // incoming
        System.out.println(ssg.toString(false)); // outgoing
        System.out.println("---------------------------------------------------------------------\n");

        List<Deque<Integer>>[] paths = ssg.pathsTo();
        System.out.println(ssg.pathsToString("all", paths));

        List<Deque<Integer>>[] from = ssg.pathsFrom();
        System.out.println(ssg.pathsToString("from", from));

        System.out.println("------------------------------- PATHS -------------------------------");
        List<Deque<Integer>> complete = ssg.getPaths();
        System.out.println(ssg.completeToString(complete));
        System.out.println("---------------------------------------------------------------------");
    }
}
