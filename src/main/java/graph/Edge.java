package graph;

public class Edge {

    private int src, dst;           // edge's source and destination
    private String label;           // edge's label (operation id)
    private boolean visited;        // indicates whether the edge was visited
    private int flow, capacity;     // edge's flow and capacity for Dinic's Algorithm
    private Edge inverse;           // edge going from dst to src

    public Edge(int src, int dst, String label, int capacity) {
        this.src = src;
        this.dst = dst;
        this.label = label;
        visited = false;
        this.capacity = capacity;
        flow = 0;
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
     * Sets the inverse edge (going from dst to src).
     *
     * @param inverse edge.
     */
    public void setInverse(Edge inverse) {
        this.inverse = inverse;
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
     * Returns the edge remaining capacity.
     *
     * @return remaining capacity.
     */
    public int getRemainingCapacity() {
        return capacity - flow;
    }

    /**
     * Increments the edge flow value by the given bottleneck.
     *
     * @param bottleneck value to increment
     */
    public void incFlow(int bottleneck) {
        flow += bottleneck;
        if (inverse != null) // edges to the super sink do not have inverse
            inverse.decFlow(bottleneck);
    }

    /**
     * Decrements the edge flow value by the given bottleneck.
     *
     * @param bottleneck value to decrement
     */
    public void decFlow(int bottleneck) {
        flow -= bottleneck;
    }

    /**
     * Checks whether the edge was already visited.
     *
     * @return true when the edge was visited; false otherwise.
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * Visits an edge.
     */
    public void visit() {
        visited = true;
    }

    /**
     * Returns the edge inverse edge.
     *
     * @return inverse edge.
     */
    public Edge getInverseEdge() {
        return inverse;
    }

    /**
     * Returns the edge's capacity.
     *
     * @return capacity
     */
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int v) {
        capacity = v;
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
//        String s = src + " -> " + dst + ", remaining: " + getRemainingCapacity() + ", label: " + label + ", inverse: ";
//
//        if (inverse != null)
//            return s + inverse.getSrc() + " -> " + inverse.getDst() + ", inv cap: " + inverse.getCapacity();
//        else
//            return s + "null";

        String s = src + " -> " + dst + ", visited: " + visited + ", label: " + label;

        return s;
    }
}
