package graph;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.nio.Attribute;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;

public class StateSpaceGraph {

    public static final String LABEL = "label";

    private final Map<String, Map<String, Attribute>> attrs;  // graph attributes; contains the state
    private final Graph<String, LabeledEdge> graph;
    private final String initialState; // first vertex id

    public StateSpaceGraph(Graph<String, LabeledEdge> graph, Map<String, Map<String, Attribute>> attrs, String id) {
        this.graph = graph;
        this.attrs = attrs;
        initialState = id;
        initialiseStateEdges();
    }

    /**
     * Gets the state of vertex v.
     *
     * @param v vertex identifier.
     * @return state.
     */
    public String getState(String v) {
        return attrs.get(v).get(LABEL).toString();
    }

    /**
     * Initialises the edge source and target with the corresponding states based on the vertex id's.
     */
    private void initialiseStateEdges() {
        for (String v : graph.vertexSet())
            for (LabeledEdge e : graph.edgeSet()) {
                if (e.sameSource(v))
                    e.setSource(getState(v));
                if (e.sameTarget(v))
                    e.setTarget(getState(v));
            }
    }

    /**
     * Edge-coverage depth first search algorithm.
     */
    public List<String> dfs() {
        List<String> sequence = new ArrayList<>();
        resetEdges();
        DepthFirstIterator<String, LabeledEdge> it = new DepthFirstIterator<>(graph);

        while (it.hasNext()) {
            String v = it.next();
            for (LabeledEdge e : graph.edgeSet())
                if (e.sameSource(v) && !e.visited()) {
                    sequence.add(e.getLabel());
                    e.visit();
                }
        }

        return sequence;
    }

    public List<String> topSort() {
        List<String> sequence = new ArrayList<>();
        resetEdges();

        TopologicalOrderIterator<String, LabeledEdge> it = new TopologicalOrderIterator<>(graph);

        while (it.hasNext()) {
            String v = it.next();
            for (LabeledEdge e : graph.edgeSet())
                if (e.sameSource(v) && !e.visited()) {
                    sequence.add(e.getLabel());
                    e.visit();
                }
        }

        return sequence;
    }

    /**
     * Returns all paths in the graph starting from the initial state (first vertex).
     *
     * @param max    max path length.
     * @param simple when true it returns all simple (non-self-intersecting) paths.
     * @return list of graph paths.
     */
    public List<List<String>> allPaths(int max, boolean simple) {
        List<List<String>> duplicates = new ArrayList<>();
        int size = 0;

        // Add the first vertex id to a set
        Set<String> initialSet = new HashSet<>(1);
        initialSet.add(initialState);

        // Retrieving all paths from the first vertex to all vertexes
        AllDirectedPaths<String, LabeledEdge> p = new AllDirectedPaths<>(graph);
        List<GraphPath<String, LabeledEdge>> graphPaths = p.getAllPaths(initialSet, graph.vertexSet(), simple, max);

        for (GraphPath<String, LabeledEdge> path : graphPaths) {
            List<LabeledEdge> edges = path.getEdgeList();
            duplicates.add(new ArrayList<>());
            for (LabeledEdge e : edges)
                duplicates.get(size).add(e.getLabel());
            size++;
        }

        // Removing duplicate paths
        return duplicates.stream().distinct().toList();
    }


    /**
     * Marks all edges as unvisited.
     */
    private void resetEdges() {
        for (LabeledEdge e : graph.edgeSet())
            e.unvisit();
    }

    /**
     * Prints the graph.
     */
    public void print() {
        for (LabeledEdge e : graph.edgeSet())
            System.out.println(e);
    }
}
