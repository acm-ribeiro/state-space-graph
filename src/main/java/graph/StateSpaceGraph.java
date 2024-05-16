package graph;

import domain.State;
import graph.exceptions.NodeNotFoundException;
import parser.VisitorOrientedParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class StateSpaceGraph {

    // DOT processing
    private static final String EDGE_CHAR = " -> ";
    private static final String LABEL = "label=";
    private static final String SPACE = " ";
    private static final String QUOTE = "\"";

    // Exceptions
    private static final String NOT_FOUND = "File %s not found.\n";

    // Edge labels
    private static final String SINK = "sink";
    private static final String INVERSE = "inv";

    private List<Edge>[] graph;
    private State[] states;
    private int[] nodeLevels;
    private Map<Long, Integer> nodesById;

    public StateSpaceGraph(String filePath) {
        nodesById = new HashMap<>();
        try {
            countNodes(filePath);    // counts the number of nodes in the graph; initialises graph, states and nodeLevels
            processEdges(filePath);  // adds the graph's edges and states - this is needed because there are edges
                                     // referencing nodes defined in posterior lines of the DOT file
            addSuperSink();
            addInverseEdges();
        } catch (FileNotFoundException e) {
            System.err.printf(NOT_FOUND, filePath);
        }
    }


    /**
     * Dinic's algorithm implementation.
     *
     * @return maximum flow value.
     * @throws NodeNotFoundException when for some reason we reach an invalid node.
     */
    public int dinic() throws NodeNotFoundException {
        int maxFlow = 0;

        while (bfs()) {
            /*
             * Shimon Even and Alon Itai's optimisation for pruning dead ends.
             * This array tracks which edge we should take next for each node, i.e., next[n] indicates the
             * next edge index to take in the adjacency list for node n.
             */
            int[] next = new int[graph.length];

            int f = dfs(0, next, Integer.MAX_VALUE);
            while (f != 0) {
                maxFlow += f;
                f = dfs(0, next, Integer.MAX_VALUE);
            }
        }

        return maxFlow;
    }

    /**
     * Constructs a level graph for Dinic's algorithm, by performing a BFS on the original graph.
     * The levels of the graph are those obtained by doing a BFS on the source node, to label all the levels
     * of the current flow graph. The level graph consists of all edges which go from L to L+1 in level and have
     * remaining capacity (flow) > 0.
     *
     * @return true if we were able to reach the sink; false otherwise.
     * @throws NodeNotFoundException if a non-existent node id is provided. If this exception is thrown, something
     *                               terrible happened.
     */
    private boolean bfs() throws NodeNotFoundException {
        // Initialises the node levels at -1
        Arrays.fill(nodeLevels, -1);

        // Stores the nodes to visit next
        Deque<Integer> q = new ArrayDeque<>(graph.length);

        // Add source node
        q.add(0);
        nodeLevels[0] = 0;

        while (!q.isEmpty()) {
            int n = q.poll(); // dequeue
            List<Edge> outgoing = graph[n];

            for (Edge e : outgoing) {
                int cap = e.getRemainingCapacity();
                int dst = e.getDst();

                if (cap > 0 && nodeLevels[dst] == -1) {
                    nodeLevels[dst] = nodeLevels[n] + 1;
                    q.offer(dst); // enqueue
                }
            }
        }

        // Return whether we were able to reach the super sink
        return nodeLevels[graph.length - 1] != -1;
    }

    /**
     * Recursive depth-first search.
     *
     * @param current the current node
     * @param next    a map indicating which edge to take next for each node
     * @param flow    the minimum flow value along the path so far (starts at positive infinity)
     * @return maximum flow along the path.
     */
    private int dfs(int current, int[] next, int flow) {
        // if we reached the sink, the algorithm should terminate
        if (current == graph.length - 1)
            return flow;

        // number of outgoing edges of the current node
        int numEdges = graph[current].size();
        int edgeIdx = next[current];

        while (edgeIdx < numEdges) {
            Edge edge = graph[current].get(edgeIdx);
            int capacity = edge.getRemainingCapacity();

            if (capacity > 0 && nodeLevels[edge.getDst()] == nodeLevels[current] + 1) {
                int bottleneck = dfs(edge.getDst(), next, Math.min(flow, capacity));

                if (bottleneck > 0) {
                    edge.incFlow(bottleneck);
                    return bottleneck;
                }
            }

            edgeIdx++;
            next[current] = edgeIdx;
        }

        return 0;
    }

    /**
     * Adds an inverse edge for all edges that do not have one.
     */
    private void addInverseEdges() {
        for (int src = 0; src < graph.length - 1; src++) {
            for (Edge e : graph[src]) {
                if (!e.getLabel().equals(SINK)) {
                    int dst = e.getDst();
                    int i = 0;
                    Edge e1, inverse = null;

                    while (inverse == null && i < graph[dst].size()) {
                        e1 = graph[dst].get(i);
                        if (e1.getDst() == src)
                            inverse = e1;
                        i++;
                    }

                    if (inverse != null) { // found
                        inverse.setInverse(e);
                        e.setInverse(inverse);
                    } else
                        e.setInverse(new Edge(dst, src, INVERSE, 0));
                }
            }
        }
    }


    /**
     * First pass through the DOT file.
     * Initialises graph and states. Populates the nodesById data structure.
     *
     * @param filePath DOT file path.
     * @throws FileNotFoundException when the DOT is not found.
     */
    private void countNodes(String filePath) throws FileNotFoundException {
        Scanner sc = new Scanner(new FileReader(filePath));
        String line;

        while (sc.hasNextLine()) {
            line = sc.nextLine();
            if (isNodeDescription(line)) {
                long id = Long.parseLong(line.split(SPACE)[0]);
                nodesById.put(id, nodesById.size());
            }
        }

        graph = new List[nodesById.size() + 1];
        states = new State[nodesById.size() + 1];
        nodeLevels = new int[nodesById.size() + 1];

        sc.close();
    }

    /**
     * Second pass through the DOT file.
     * Processes the graph's edges. Fills the graph data structure.
     *
     * @param filePath DOT file path.
     * @throws FileNotFoundException when the DOT is not found.
     */
    private void processEdges(String filePath) throws FileNotFoundException {
        VisitorOrientedParser parser = new VisitorOrientedParser();
        Scanner sc = new Scanner(new FileReader(filePath));
        String line;
        Edge edge;

        while (sc.hasNextLine()) {
            line = sc.nextLine();

            if (isEdgeDescription(line)) {
                long src = Long.parseLong(line.split(EDGE_CHAR)[0]);
                long dst = Long.parseLong(line.split(EDGE_CHAR)[1].trim().split(SPACE)[0]);
                String label = line.split(LABEL)[1].split(QUOTE)[1];

                int srcId = nodesById.get(src);
                int dstId = nodesById.get(dst);
                edge = new Edge(srcId, dstId, label, 1);

                if (graph[srcId] == null)
                    graph[srcId] = new ArrayList<>();

                if (!graph[srcId].contains(edge))
                    graph[srcId].add(edge);
            } else if (isNodeDescription(line)) {
                State state = parser.parse(line.split(QUOTE)[1]);
                long id = Long.parseLong(line.split(SPACE)[0]);
                int nodeId = nodesById.get(id);
                states[nodeId] = state;
            }
        }

        sc.close();
    }

    /**
     * Adds an edge from all the final states to the super sink node.
     */
    private void addSuperSink() {
        graph[graph.length - 1] = new ArrayList<>();

        for (int i = 0; i < graph.length; i++)
            if (graph[i] == null) {
                graph[i] = new ArrayList<>();
                graph[i].add(new Edge(i, graph.length - 1, SINK, Integer.MAX_VALUE));
            }
    }

    /**
     * Checks whether a DOT file line corresponds to a node description.
     *
     * @param line DOT file line.
     * @return true if the line is a node description; false otherwise.
     */
    private boolean isNodeDescription(String line) {
        return line.contains(LABEL) && !line.contains(EDGE_CHAR);
    }

    /**
     * Checks whether a DOT file line corresponds to an edge description.
     *
     * @param line DOT file line.
     * @return true if the line is an edge description; false otherwise.
     */
    private boolean isEdgeDescription(String line) {
        return line.contains(EDGE_CHAR);
    }

    /**
     * Returns a string representation of the graph with detailed information on the edges.
     * @return string representation of the graph
     */
    public String detailedEdges() {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < graph.length; i++) {
            s.append(i);
            s.append(": \n");

            for (Edge e : graph[i]) {
                s.append("   ");
                s.append(e);
                s.append("\n");
            }
        }

        return s.toString();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < graph.length; i++) {
            s.append(i);
            s.append(": {");

            for (Edge e : graph[i]) {
                s.append(e.getDst());
                s.append("; ");
            }

            if (!graph[i].isEmpty())
                s.delete(s.length() - 2, s.length());

            s.append("}\n");
        }

        return s.toString();
    }
}
