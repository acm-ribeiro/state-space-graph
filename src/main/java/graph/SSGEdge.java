package graph;

public class SSGEdge {

    private int src, dst;           // edge's source and destination
    private String label;           // edge's label (operation id)
    private boolean visited;        // indicates whether the edge was visited
    private int flow, capacity;     // edge's flow and capacity for Dinic's Algorithm
    private Edge inverse;           // edge going from dst to src

    public SSGEdge(int src, int dst, String label) {
        this.src = src;
        this.dst = dst;
        this.label = label;
        visited = false;
        capacity = 1;
        flow = 0;
    }

    public int getSrc() {
        return src;
    }

    public int getDst() {
        return dst;
    }

    public String getLabel() {
        return label;
    }

    public boolean isVisited() {
        return visited;
    }

    public int getFlow() {
        return flow;
    }

    public int getCapacity() {
        return capacity;
    }

    public Edge getInverse() {
        return inverse;
    }
}
