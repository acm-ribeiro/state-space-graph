package org.example;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTImporter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;


public class Main {

    private static final String DOT_FILE = "dot_files/OneBitClock.dot";
    private static final String LABEL = "label";

    public static void main(String[] args) throws FileNotFoundException {
        Graph<String, LabeledEdge> dag = new SimpleDirectedGraph<>(LabeledEdge.class);

        Graph<String, LabeledEdge> result = new SimpleDirectedGraph<>(LabeledEdge.class);

        DOTImporter<String, LabeledEdge> dotImporter = new DOTImporter<>();
        //dotImporter.setVertexWithAttributesFactory(id, label -> new StateVertex(label.toString()));
        dotImporter.setVertexFactory(label -> label);
        dotImporter.setEdgeWithAttributesFactory(label -> new LabeledEdge(label.get(LABEL).toString()));

        Map<String, Map<String, Attribute>> attrs = new HashMap<>();
        dotImporter.addVertexAttributeConsumer((p, a) -> {
            Map<String, Attribute> map = attrs.computeIfAbsent(p.getFirst(), k -> new HashMap<>());
            map.put(p.getSecond(), a);
        });

        dotImporter.importGraph(dag, new FileReader(DOT_FILE));

        // VERTEXES
        System.out.println("vertexes");
        for (String v : dag.vertexSet())
            System.out.println(attrs.get(v).get(LABEL));

        // EDGES
        System.out.println("edges");
        for(LabeledEdge e : dag.edgeSet()) {
            System.out.println(e);
        }
    }
}