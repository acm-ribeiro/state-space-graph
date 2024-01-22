package org.example;

import graph.LabeledEdge;
import graph.StateSpaceGraph;
import graph.StateVertex;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTImporter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;



public class Main {



    public static void main(String[] args) throws FileNotFoundException {
       StateSpaceGraph dag = fromDOT(args[0]);
       dag.print();
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