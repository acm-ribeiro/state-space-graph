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
    private static final String FINAL = "final";

    // Initial sizes
    private static int INITIAL_NODES = 1000;
    private static int INITIAL_EDGES = 30;

    // Initial state index
    private static int INITIAL = 0;

    private List<Edge>[] outgoing;          // stores the outgoing edges of all the graph's nodes
    private List<Edge>[] incoming;          // stores the incoming edges of all the graph's nodes
    private State[] states;                 // stores the graph states
    private Map<Long, Integer> nodesById;   // stores the graph's nodes by id
    private int finalState;                 // stores the graph's final state index
    private List<Integer>[] prev;           // stores the parents of a node in the BFS tree

    public StateSpaceGraph(String filePath) {
        nodesById = new HashMap<>(INITIAL_NODES);
        try {
            countNodes(filePath);
            initialiseStructures();
            processEdges(filePath);
            addFinalState();
        } catch (IOException e) {
            System.err.printf(NOT_FOUND, filePath);
        }
    }

    /**
     * Returns the state of a given node.
     *
     * @param idx node index.
     * @return state.
     */
    public State getState(int idx) {
        return states[idx];
    }

    /**
     * Returns the graph's initial state.
     *
     * @return initial state.
     */
    public State getInitialState() {
        return states[INITIAL];
    }

    /**
     * Returns the paths on the SSG.
     *
     * @return nominal paths.
     */
    public List<List<Edge>> getPaths() {
        List<List<Integer>> paths = completePaths();
        List<List<Edge>> nominal = new ArrayList<>(paths.size());

        ArrayList<Edge> n;
        for (List<Integer> p : paths) {
            n = new ArrayList<>(p.size() - 1); // 4 nodes = 3 transitions

            for (int i = 0; i < p.size() - 2; i++)  // -2 to remove the sink
                n.add(findEdge(outgoing[p.get(i)], p.get(i + 1)));

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
    private List<List<Integer>> twistedBFS() {
        // Tracks whether nodes have been expanded
        boolean[] expanded = new boolean[outgoing.length];

        // Stores the nodes to visit next
        Deque<Integer> q = new ArrayDeque<>(outgoing.length);
        q.offer(INITIAL);

        // Stores the paths found so far (including incomplete paths)
        List<List<Integer>> paths = new LinkedList<>();

        // Path with the initial node
        List<Integer> path = new LinkedList<>();
        path.add(INITIAL);
        paths.add(path);

        int node, next;

        while (!q.isEmpty()) {
            node = q.poll();
            if (!expanded[node]) {
                List<Edge> out = outgoing[node];

                for (Edge e : out) {
                    if (!e.isVisited()) {
                        next = e.getDst();
                        e.visit();
                        q.offer(next);
                        prev[next].add(node);

                        path = findPath(paths, node);  // TODO there may be a better way of doing this.

                        if (path != null) {
                            // Cloning for path extension
                            List<Integer> cpy = new LinkedList<>(path);
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
    private List<List<Integer>> completePaths() {
        List<List<Integer>> incomplete = twistedBFS();

        /* Stores the partial paths from the node index to the final state
         * e.g., in the smallest graph:
         * partial[0] = [ [0, 1, 3, 9], [0, 2, 6, 9] ]
         * partial[1] = [ [1, 3, 9], [1, 5, 8, 3, 9] ]
         */
        List<List<Integer>>[] partial = initialisePartialPaths();

        // Stores the nodes to extend next. The first node to extend is the final state.
        Deque<Integer> q = new ArrayDeque<>(outgoing.length);
        q.offer(outgoing.length - 1);

        // Tracks whether nodes have been expanded
        boolean[] expanded = new boolean[outgoing.length];

        /* Tracks the current path we're extending in the partial paths list.
         * e.g., if currentPathIdx[0] = 1 we know partial[0].size() = 2 and partial[0].get(0)
         * has a partial path from the initial state (0) to the final state (graph.length - 1).
         */
        int[] currentPathIdx = new int[outgoing.length];

        // Node being currently expanded
        int node;
        List<Integer> nodePrev, path;

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
        int i = 0, completePaths = 0, last;

        while (completePaths < incomplete.size()) {
            if (!complete[i]) {
                path = incomplete.get(i);
                last = path.get(path.size() - 1);
                path.addAll(partial[last].get(0)); // As of now this works. The number of paths can "explode".
                complete[i] = last == finalState;

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
    private boolean isComplete(List<Integer> path) {
        return path.get(path.size() - 1).equals(finalState);
    }

    /**
     * Removes the first element of every partial path.
     *
     * @param partial partial paths list.
     */
    private void cleanPartial(List<List<Integer>>[] partial) {
        for (List<List<Integer>> pathList : partial)
            for (List<Integer> path : pathList)
                path.remove(0);
    }

    /**
     * Initialises the partial paths structure used to complete the paths resulting from the Twisted BFS.
     *
     * @return partial paths.
     */
    @SuppressWarnings("unchecked")
    private List<List<Integer>>[] initialisePartialPaths() {
        List<List<Integer>>[] partial = new LinkedList[outgoing.length];
        List<Integer> first;

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
    private List<Integer> findPath(List<List<Integer>> paths, int value) {
        List<Integer> path, found = null;
        int i = 0;

        while (found == null && i < paths.size()) {
            path = paths.get(i);
            if (path.get(path.size() - 1) == value)
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
    @SuppressWarnings("unchecked")
    private void initialiseStructures() {
        outgoing = new List[nodesById.size() + 1]; // warning
        for (int i = 0; i < outgoing.length; i++)
            outgoing[i] = new ArrayList<>(INITIAL_EDGES);

        prev = new List[outgoing.length];
        for (int i = 0; i < prev.length; i++)
            prev[i] = new LinkedList<>();

        states = new State[outgoing.length];
        finalState = outgoing.length - 1;
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

                if (!outgoing[srcId].contains(edge))
                    outgoing[srcId].add(edge);

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
    private void addFinalState() {
       for (int i = 0; i < outgoing.length - 1; i++)
            if (outgoing[i].isEmpty())
                outgoing[i].add(new Edge(i, finalState, FINAL, Integer.MAX_VALUE));
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

        for (int i = 0; i < outgoing.length; i++) {
            s.append(i);
            s.append(": \n");

            for (Edge e : outgoing[i]) {
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

        for (int i = 0; i < outgoing.length; i++) {
            s.append(i);
            s.append(": {");

            for (Edge e : outgoing[i]) {
                s.append(e.getDst());
                s.append("; ");
            }

            if (!outgoing[i].isEmpty())
                s.delete(s.length() - 2, s.length());

            s.append("}\n");
        }

        return s.toString();
    }
}
