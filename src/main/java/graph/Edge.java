package graph;

public class Edge {

    private long src, tgt;
    private String label;

    public Edge(long src, long tgt, String label) {
        this.src = src;
        this.tgt = tgt;
        this.label = label;
    }

    public long getSrc() {
        return src;
    }

    public long getTgt() {
        return tgt;
    }

    public String getLabel() {
        return label;
    }
}
