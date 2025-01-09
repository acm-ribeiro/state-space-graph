package graph;

import java.util.Arrays;

public class Path {

    private static final int PATHS_SIZE = 3000;

    private int node;       // node where the path starts
    private Path[] from;    // paths from this node

    public Path(int node) {
        this.node = node;
        from = new Path[PATHS_SIZE];
    }

    /**
     * Returns the first node in the path.
     *
     * @return node.
     */
    public int getNode() {
        return node;
    }

    /**
     * Adds a new path from this node.
     *
     * @param path path from node.
     */
    public void addFromPath(int[] path) {

    }

    /**
     * Returns all paths from this node.
     *
     * @return paths from node.
     */
    public Path[] getFrom() {
        return from;
    }


    /**
     * Resizes the paths array to have an additional position.
     * Only one position is added because space is an issue.
     */
    private void resizePaths() {
        from = Arrays.copyOf(from, from.length + 1);
    }

}
