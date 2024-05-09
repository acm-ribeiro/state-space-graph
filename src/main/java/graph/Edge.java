package graph;

public class Edge {

    private String id;              // edge ID
    private long src, tgt;          // source and target vertices' ID
    private String label;           // edge label
    private int capacity, flow;     // edge maximum capacity and current flow
    private String residual;        // residual edge id

    public Edge (String id, long src, long tgt, String label, int capacity) {
        this.id = id;
        this.src = src;
        this.tgt = tgt;
        this.label = label;
        this.capacity = capacity;
        flow = 0;
    }

    /**
     * Returns the edge ID.
     *
     * @return edge ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets this edge's residual edge.
     * The residual edge has capacity 0. It is only used to backtrack, so no flow can go through it.
     *
     * @param residualId residual edge's id
     */
    public void setResidual (String residualId) {
        residual = residualId;
    }

    /**
     * Returns the edge's residual edge.
     *
     * @return residual edge.
     */
    public String getResidualId () {
        return residual;
    }

    /**
     * Returns the id of this edge's source vertex.
     *
     * @return the source vertex id
     */
    public long getSrc () {
        return src;
    }

    /**
     * Returns the id of this edge's target vertex.
     *
     * @return the source target id
     */
    public long getTgt () {
        return tgt;
    }

    /**
     * Returns this edge's maximum capacity.
     *
     * @return edge's maximum capacity.
     */
    public int getCapacity () {
        return capacity;
    }

    /**
     * Returns the current flow going through this edge.
     *
     * @return current flow value.
     */
    public int getFlow () {
        return flow;
    }

    /**
     * Returns the edge's remaining capacity.
     * The remaining capacity of an edge is the difference between its total capacity and the current flow value.
     *
     * @return remaining capacity.
     */
    public int getRemainingCapacity () {
        return capacity - flow;
    }

    /**
     * Increments the flow going through this edge.
     *
     * @param bottleneck value to increment.
     * @pre residual != null
     */
    public void incFlow (int bottleneck) {
        flow += bottleneck;
    }

    /**
     * Decrements the flow going through this edge.
     *
     * @param bottleneck value to decrement.
     */
    public void decFlow (int bottleneck) {
        flow -= bottleneck;
    }

    /**
     * Returns the edge's label.
     *
     * @return  edge's label.
     */
    public String getLabel () {
        return label;
    }

    /**
     * Checks whether an edge is residual.
     *
     * @return true if the edge is residual; false otherwise.
     */
    public boolean isResidual () {
        return capacity == 0;
    }
}
