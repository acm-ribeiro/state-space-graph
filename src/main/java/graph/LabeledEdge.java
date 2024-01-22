package graph;

import org.jgrapht.graph.DefaultEdge;

public class LabeledEdge extends DefaultEdge {

    private String label;
    private String src, tgt;

    /**
     * Creates a new Labeled Edge instance.
     *
     * @param label  edge label - operation.
     * @param src    edge source state
     * @param tgt    edge target state
     */
    public LabeledEdge(String label) {
        this.label = label;
        this.src = "";
        this.tgt = "";
    }

    public String getLabel() {
        return label;
    }


    public String getStateSource() {
        return src;
    }

    public String getStateTarget() {
       return tgt;
    }

    public void setSource(String src) {
        this.src = src;
    }

    /**
     * Checks whether the edge has the same vertex source as the given vertex id.
     *
     * @param v vertex id.
     * @return true when the edge source is the vertex with the give id; false otherwise.
     */
    public boolean sameSource (String v) {
        return getSource().equals(v);
    }

    /**
     * Checks whether the edge has the same vertex target as the given vertex id.
     *
     * @param v vertex id.
     * @return true when the edge target is the vertex with the give id; false otherwise.
     */
    public boolean sameTarget (String v) {
        return getTarget().equals(v);
    }

    public void setTarget(String tgt) {
        this.tgt = tgt;
    }

    @Override
    public String toString() {
        return src + " -- " + label + " --> " + tgt;
    }
}
