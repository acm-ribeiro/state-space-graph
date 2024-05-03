package graph;

public class Edge {

    private long src, tgt;          // source and target vertices' id
    private String label;           // edge label
    private int capacity, flow;     // edge maximum capacity and current flow

    public Edge(long src, long tgt, String label) {
        this.src = src;
        this.tgt = tgt;
        this.label = label;
        capacity = 1;
        flow = 0;
    }

    /**
     * Returns the id of this edge's source vertex.
     *
     * @return the source vertex id
     */
    public long getSrc() {
        return src;
    }

    /**
     * Returns the id of this edge's target vertex.
     *
     * @return the source target id
     */
    public long getTgt() {
        return tgt;
    }

    /**
     * Returns this edge's maximum capacity.
     *
     * @return edge's maximum capacity.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Returns the current flow going through this edge.
     *
     * @return current flow value.
     */
    public int getFlow() {
        return flow;
    }

    /**
     * Returns the edge's remaining capacity.
     * The remaining capacity of an edge is the difference between its total capacity and the current flow value.
     *
     * @return remaining capacity.
     */
    public int getRemainingCapacity() {
        return capacity - flow;
    }

    /**
     * Increments the flow going through this edge.
     *
     * @param val value to increment.
     * @return true if the increment was successful; false otherwise.
     */
    public boolean incFlow (int val) {
        if(flow + val <= capacity) {
            flow += val;
            return true;
        }

        return false;
    }

    /**
     * Returns the edge's label.
     *
     * @return  edge's label.
     */
    public String getLabel() {
        return label;
    }
}
