package org.example;

import graph.LabeledEdge;
import graph.StateSpaceGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.traverse.NotDirectedAcyclicGraphException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        StateSpaceGraph graph = fromDOT(args[0]);
        graph.print();

        List<String> seq = graph.dfs(); // calls to the DFS algorithm always return the same sequence.
        String dfs = "";
        for (String s : seq)
            dfs += s;
        System.out.println(dfs);

        try {
            List<String> seq1 = graph.topSort(); // calls to the DFS algorithm always return the same sequence.
            String topSort = "";
            for (String s : seq1)
                topSort += s;
            System.out.println(topSort);
        } catch (NotDirectedAcyclicGraphException e) {
            System.err.println("Graph is not acyclic.");
        }

    }

    /**
     * Imports a graph from a .dot file.
     *
     * @param dot file location.
     */
    private static StateSpaceGraph fromDOT(String dot) throws FileNotFoundException {
        Graph<String, LabeledEdge> dag = new SimpleDirectedGraph<>(LabeledEdge.class);

        DOTImporter<String, LabeledEdge> dotImporter = new DOTImporter<>();
        dotImporter.setVertexFactory(label -> label);
        dotImporter.setEdgeWithAttributesFactory(label -> new LabeledEdge(label.get(StateSpaceGraph.LABEL).toString()));

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        dotImporter.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.computeIfAbsent(p.getFirst(), k -> new HashMap<>());
            map.put(p.getSecond(), a);
        });

        dotImporter.importGraph(dag, new FileReader(dot));

        return new StateSpaceGraph(dag, attrs);
    }
}