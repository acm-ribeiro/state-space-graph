package graph;

import domain.State;
import parser.VisitorOrientedParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

    // Initial sizes
    private static int INITIAL_NODES = 1000;
    private static int INITIAL_EDGES = 30;

    private List<Edge>[] graph;
    private State[] states;
    private Map<Long, Integer> nodesById;

    private int[] nodeLevels, next;        // for Dinic's algorithm
    private LinkedList<Integer>[] prev;    // stores the parents of a node in the BFS tree

    public StateSpaceGraph(String filePath) {
        nodesById = new HashMap<>(INITIAL_NODES);
        try {
            countNodes(filePath);   // counts the number of nodes in the graph; initialises graph, states and nodeLevels
            initialiseStructures();
            processEdges(filePath); // adds the graph's edges and states - this is needed because there are edges
            // referencing nodes defined in posterior lines of the DOT file
            addSuperSink();
            addInverseEdges();
        } catch (IOException e) {
            System.err.printf(NOT_FOUND, filePath);
        }
    }

    /**
     * Returns the paths on the SSG.
     *
     * @return nominal paths.
     */
    public List<List<Edge>> getPaths() {
        List<LinkedList<Integer>> paths = completePaths();
        List<List<Edge>> nominal = new ArrayList<>(paths.size());

        ArrayList<Edge> n;
        for (LinkedList<Integer> p : paths) {
            n = new ArrayList<>(p.size() - 1); // 4 nodes = 3 transitions

            for (int i = 0; i < p.size() - 2; i++)  // -2 to remove the sink
                n.add(findEdge(graph[p.get(i)], p.get(i + 1)));

            nominal.add(n);
        }

        return nominal;
    }

    /**
     * A breadth-first search with a twist: a node is only expanded once.
     * TODO we need a better name for this.
     *
     * @return possibly incomplete graph paths.
     */
    private List<LinkedList<Integer>> twistedBFS() {
        // Tracks whether nodes have been expanded
        boolean[] expanded = new boolean[graph.length];

        // Stores the nodes to visit next
        Deque<Integer> q = new ArrayDeque<>(graph.length);
        q.offer(0);

        // Stores the paths found so far (including incomplete paths)
        List<LinkedList<Integer>> paths = new LinkedList<>();

        // Path with the initial node
        LinkedList<Integer> path = new LinkedList<>();
        path.add(0);
        paths.add(path);

        int node, next;

        while (!q.isEmpty()) {
            node = q.poll();
            if (!expanded[node]) {
                List<Edge> out = graph[node];

                for (Edge e : out) {
                    if (!e.isVisited()) {
                        next = e.getDst();
                        e.visit();
                        q.offer(next);
                        prev[next].add(node);

                        path = findPath(paths, node);  // TODO there may be a better way of doing this.

                        if (path != null) {
                            // Cloning for path extension
                            LinkedList<Integer> cpy = new LinkedList<>(path);
                            cpy.add(next);
                            paths.add(cpy);
                        } else {
                            // Creating a new path
                            path = new LinkedList<>();
                            path.add(node);
                            path.add(next);
                            paths.add(path);
                        }
                    }
                }
                expanded[node] = true;

                // When a node is fully expanded we remove the path up until that node form the paths list
                paths.remove(path);
            }
        }

        return paths;
    }

    /**
     * Completes the paths resulting from the twisted BFS.
     *
     * @return complete paths.
     */
    private List<LinkedList<Integer>> completePaths() {
        List<LinkedList<Integer>> incomplete = twistedBFS();

        /* Stores the partial paths from the node index to the final state
         * e.g., in the smallest graph:
         * partial[0] = [ [0, 1, 3, 9], [0, 2, 6, 9] ]
         * partial[1] = [ [1, 3, 9], [1, 5, 8, 3, 9] ]
         */
        List<LinkedList<Integer>>[] partial = initialisePartialPaths();

        // Stores the nodes to extend next. The first node to extend is the final state.
        Deque<Integer> q = new ArrayDeque<>(graph.length);
        q.offer(graph.length - 1);

        // Tracks whether nodes have been expanded
        boolean[] expanded = new boolean[graph.length];

        /* Tracks the current path we're extending in the partial paths list.
         * e.g., if currentPathIdx[0] = 1 we know partial[0].size() = 2 and partial[0].get(0)
         * has a partial path from the initial state (0) to the final state (graph.length - 1).
         */
        int[] currentPathIdx = new int[graph.length];

        // Node being currently expanded
        int node;
        LinkedList<Integer> nodePrev, path;

        while (!q.isEmpty()) {
            node = q.poll();

            if (!expanded[node]) {
                nodePrev = prev[node];

                for (Integer i : nodePrev) {
                    path = partial[i].get(currentPathIdx[i]);
                    path.addAll(partial[node].get(0)); // We could change this index for more variety.
                    q.offer(i);

                    // If the path is complete we should move on to another list
                    if (isComplete(path)) {
                        currentPathIdx[i]++;
                        path = new LinkedList<>();
                        path.add(i);
                        partial[i].add(path);
                    }
                }

                expanded[node] = true;
            }
        }

        // Removing the first element of every partial path
        cleanPartial(partial);

        // Completing all paths; this array tracks which paths are complete
        boolean[] complete = new boolean[incomplete.size()];
        int i = 0, completePaths = 0;

        while (completePaths < incomplete.size()) {
            if (!complete[i]) {
                path = incomplete.get(i);
                path.addAll(partial[path.getLast()].get(0)); // As of now this works. The number of paths can "explode".
                complete[i] = path.getLast() == graph.length - 1;

                if (complete[i])
                    completePaths++;
            }

            i = i == incomplete.size() - 1 ? 0 : i + 1;
        }

        return incomplete;
    }

    /**
     * Checks whether a path is complete, i.e., reaches the final state.
     *
     * @param path path to check.
     * @return true if the path is complete; false otherwise.
     */
    private boolean isComplete(LinkedList<Integer> path) {
        return path.getLast().equals(graph.length - 1);
    }

    /**
     * Removes the first element of every partial path.
     *
     * @param partial partial paths list.
     */
    private void cleanPartial(List<LinkedList<Integer>>[] partial) {
        for (List<LinkedList<Integer>> pathList : partial)
            for (LinkedList<Integer> path : pathList)
                path.removeFirst();
    }

    /**
     * Initialises the partial paths structure used to complete the paths resulting from the Twisted BFS.
     *
     * @return partial paths.
     */
    private List<LinkedList<Integer>>[] initialisePartialPaths() {
        List<LinkedList<Integer>>[] partial = new LinkedList[graph.length];
        LinkedList<Integer> first;

        for (int i = 0; i < partial.length; i++) {
            first = new LinkedList<>();
            first.add(i);
            partial[i] = new LinkedList<>();
            partial[i].add(first);
        }

        return partial;
    }

    /**
     * Finds the path to extend, i.e., the path in which the last element is the same as the
     * given value.
     *
     * @param paths path list
     * @param value tail value
     * @return path
     */
    private LinkedList<Integer> findPath(List<LinkedList<Integer>> paths, int value) {
        LinkedList<Integer> found = null;
        int i = 0;

        while (found == null && i < paths.size()) {
            if (paths.get(i).getLast() == value)
                found = paths.get(i);
            i++;
        }

        return found;
    }

    /**
     * Finds an edge in a node's outgoing edge's list.
     *
     * @param out  outgoing edges of a node.
     * @param dst  edge's destination node.
     * @return edge or null if it does not exist.
     */
    private Edge findEdge(List<Edge> out, int dst) {
        Edge e, edge = null;
        int i = 0;

        while (edge == null && i < out.size()) {
            e = out.get(i);

            if (e.getDst() == dst)
                edge = e;

            i++;
        }

        return edge;
    }


    // Graph construction

    /**
     * Initialises the graph, state, nodeLevels and next arrays.
     */
    private void initialiseStructures() {
        graph = new List[nodesById.size() + 1];
        for (int i = 0; i < graph.length; i++)
            graph[i] = new ArrayList<>(INITIAL_EDGES);

        next = new int[graph.length];
        states = new State[graph.length];
        nodeLevels = new int[graph.length];

        prev = new LinkedList[graph.length];
        for (int i = 0; i < prev.length; i++)
            prev[i] = new LinkedList<>();
    }

    /**
     * First pass through the DOT file.
     * Initialises graph and states. Populates the nodesById data structure.
     *
     * @param filePath DOT file path.
     * @throws FileNotFoundException when the DOT is not found.
     */
    private void countNodes(String filePath) throws IOException {
        BufferedReader buff = new BufferedReader(new FileReader(filePath));
        String line = buff.readLine();

        while (line != null) {
            if (isNodeDescription(line))
                nodesById.put(Long.parseLong(line.split(SPACE)[0]), nodesById.size());
            line = buff.readLine();
        }

        buff.close();
    }

    /**
     * Second pass through the DOT file.
     * Processes the graph's edges. Fills the graph data structure.
     *
     * @param filePath DOT file path.
     */
    private void processEdges(String filePath) throws IOException {
        VisitorOrientedParser parser = new VisitorOrientedParser();
        BufferedReader buff = new BufferedReader(new FileReader(filePath));
        String line = buff.readLine();
        Edge edge;

        while (line != null) {
            if (isEdgeDescription(line)) {
                String[] splitByEdge = line.split(EDGE_CHAR);
                long src = Long.parseLong(splitByEdge[0]);
                long dst = Long.parseLong(splitByEdge[1].trim().split(SPACE)[0]);
                String label = line.split(LABEL)[1].split(QUOTE)[1];

                int srcId = nodesById.get(src);
                int dstId = nodesById.get(dst);
                edge = new Edge(srcId, dstId, label, 1);

                if (!graph[srcId].contains(edge))
                    graph[srcId].add(edge);

            } else if (isNodeDescription(line)) {
                State state = parser.parse(line.split(QUOTE)[1]);
                long id = Long.parseLong(line.split(SPACE)[0]);
                int nodeId = nodesById.get(id);
                states[nodeId] = state;
            }

            line = buff.readLine();
        }

        buff.close();
    }

    /**
     * Adds an edge from all the final states to the super sink node.
     */
    private void addSuperSink() {
        graph[graph.length - 1] = new ArrayList<>(INITIAL_EDGES);

        for (int i = 0; i < graph.length - 1; i++)
            if (graph[i].isEmpty())
                graph[i].add(new Edge(i, graph.length - 1, SINK, Integer.MAX_VALUE));
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


    // Dinic's Algorithm

    /**
     * Dinic's algorithm implementation.
     *
     * @return maximum flow value.
     */
    private int dinic() {
        int maxFlow = 0;

        while (dinicBfs()) {
            /*
             * Shimon Even and Alon Itai's optimisation for pruning dead ends.
             * This array tracks which edge we should take next for each node, i.e., next[n] indicates the
             * next edge index to take in the adjacency list for node n.
             */
            Arrays.fill(next, 0);
            int f = dfs(0, Integer.MAX_VALUE);
            while (f != 0) {
                maxFlow += f;
                f = dfs(0, Integer.MAX_VALUE);
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
     */
    private boolean dinicBfs() {
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
     * @param flow    the minimum flow value along the path so far (starts at positive infinity)
     * @return maximum flow along the path.
     */
    private int dfs(int current, int flow) {
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
                int bottleneck = dfs(edge.getDst(), Math.min(flow, capacity));

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

                    if (inverse == null) // not found
                        inverse = new Edge(dst, src, INVERSE, 0);

                    inverse.setInverse(e);
                    e.setInverse(inverse);
                }
            }
        }
    }


    // Debugging

    /**
     * Returns a string representation of the prev data structure.
     *
     * @return string of prev.
     */
    public String prevToString() {
        StringBuilder s = new StringBuilder("prev: \n");

        for (int i = 0; i < prev.length; i++) {
            s.append("[").append(i).append("]: ");

            if (!prev[i].isEmpty())
                for (Integer j : prev[i])
                    s.append(j).append(" -> ");

            s.append("/").append("\n");
        }

        return s.toString();
    }

    /**
     * Returns a string representation of the graph with detailed information on the edges.
     *
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
