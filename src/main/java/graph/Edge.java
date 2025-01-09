package graph;

public class Edge {

    private int src, dst;               // edge's source and destination
    private String label;           // edge's id and label (operation id)

    public Edge(int src, int dst, String label) {
        this.src = src;
        this.dst = dst;
        this.label = label;
    }

    /**
     * Returns the edge source node.
     *
     * @return source node id.
     */
    public int getSrc() {
        return src;
    }


    /**
     * Returns the edge destination node.
     *
     * @return destination node id.
     */
    public int getDst() {
        return dst;
    }

    /**
     * Returns the edge label.
     *
     * @return edge label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Checks whether the edge is equal to the given edge.
     * Two edges are equal if they share the same source and destination.
     *
     * @param object edge to compare.
     * @return true if they're equal; false otherwise
     */
    @Override
    public boolean equals(Object object) {
        return object instanceof Edge other && src == other.getSrc() && dst == other.getDst();
    }

    @Override
    public String toString() {
        return src + " -> " + dst + ", label: " + label;
    }
}
