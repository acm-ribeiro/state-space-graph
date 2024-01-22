package graph;

import org.jgrapht.Graph;
import org.jgrapht.nio.Attribute;

import java.util.Map;

public class StateSpaceGraph {

    public static final String LABEL = "label";

    private Map<String, Map<String, Attribute>> attrs;  // graph attributes; contains the state
    private Graph<String, LabeledEdge> dag;

    public StateSpaceGraph(Graph<String, LabeledEdge> dag, Map<String, Map<String, Attribute>> attrs) {
        this.dag = dag;
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
        for (String v : dag.vertexSet())
            for (LabeledEdge e : dag.edgeSet()) {
                if (e.sameSource(v))
                    e.setSource(getState(v));
                if (e.sameTarget(v))
                    e.setTarget(getState(v));
            }
    }

    /**
     * Prints the graph.
     */
    public void print() {
        for (LabeledEdge e : dag.edgeSet())
            System.out.println(e);
    }
}
