package graph;

import org.jgrapht.Graph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StateSpaceGraph {

    public static final String LABEL = "label";

    private final Map<String, Map<String, Attribute>> attrs;  // graph attributes; contains the state
    private final Graph<String, LabeledEdge> graph;

    public StateSpaceGraph(Graph<String, LabeledEdge> graph, Map<String, Map<String, Attribute>> attrs) {
        this.graph = graph;
        this.attrs = attrs;
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
