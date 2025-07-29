package graph;

import domain.State;
import parser.VisitorOrientedParser;
import pruning.PathPruner;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class StateSpaceGraph {

    // Debug
    private static final String NODES = "------------------------- NODES -------------------------";
    private static final String EDGES = "------------------------- EDGES -------------------------";
    private static final String PATHS = "------------------------- PATHS -------------------------";
    private static final String GRAPH = "------------------------- GRAPH -------------------------";
    private static final String STATS = "------------------------- STATS -------------------------";
    private static final String SPLIT = "---------------------------------------------------------";

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
    private static int INITIAL_PARAMS = 10;

    // Complete & incomplete paths
    private static final int PATH_CATEGORIES = 2;
    private static final int COMPLETE = 0;
    private static final int INCOMPLETE = 1;

    // A graph with 2k nodes results in 2M paths.
    private static final int NUM_PATHS = 10000;

    // It's common to have paths sizes ranging from 20-30
    private static final int PATH_LENGTH = 30;

    // Initial state index
    private static int INITIAL = 0;

    private int finalState;            // Final state index
    private int numNodes;             // Number of nodes in the graph
    private int numEdges;             // Number of edges in the graph
    private List<Edge>[] outgoing;    // Outgoing edges of all the graph's nodes
    private List<Edge>[] incoming;    // Incoming edges of all the graph's nodes
    private State[] states;           // TLA+ states

    private Map<Long, Integer> nodesById;
    private Map<String, Edge> edgesById;

    public StateSpaceGraph(String filePath) {
        nodesById = new HashMap<>(INITIAL_NODES);
        try {
            countNodes(filePath);
            initialiseStructures();
            processEdges(filePath);
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
     * Returns the number of nodes in the graph.
     *
     * @return number of nodes.
     */
    public int getNumNodes() {
        return numNodes;
    }

    /**
     * Returns the number of edges in the graph.
     *
     * @return number of edges.
     */
    public int getNumEdges() {
        return numEdges;
    }

    /**
     * Returns the graph's initial state.
     *
     * @return initial state.
     */
    public State getInitialState() {
        return states[INITIAL];
    }

    // Graph Traversal

    /**
     * A modified version of the standard BFS traversal, starting from the
     * initial state, returning a two-position array with a list of complete and incomplete
     * paths.
     *
     * @return both complete and incomplete paths.
     */
    @SuppressWarnings("unchecked")
    private List<Deque<Integer>>[] pathsTo() {
        // Tracks whether nodes have been added to the FIFO
        boolean[] found = new boolean[numNodes];
        found[finalState] = true;

        Deque<Integer> fifo = new ArrayDeque<>(numNodes);
        fifo.offer(INITIAL);
        found[INITIAL] = true;

        List<Deque<Integer>>[] paths = new List[PATH_CATEGORIES];
        paths[COMPLETE] = new LinkedList<>();
        paths[INCOMPLETE] = new LinkedList<>();

        Deque<Integer>[] upTo = new Deque[numNodes];
        for (int i = 0; i < numNodes; i++)
            upTo[i] = new ArrayDeque<>(PATH_LENGTH);
        upTo[INITIAL].add(INITIAL);

        int parent, child;
        Deque<Integer> upToChild;
        while (!fifo.isEmpty()) {
            parent = fifo.poll();

            for (Edge e : outgoing[parent]) {
                child = e.getDst();
                upToChild = new ArrayDeque<>(upTo[parent]);
                upToChild.offer(child);

                if (!found[child]) {
                    fifo.offer(child);
                    found[child] = true;
                    upTo[child] = upToChild;
                } else if (child == finalState) {
                    paths[COMPLETE].add(upToChild);
                } else {
                    paths[INCOMPLETE].add(upToChild);
                }
            }
        }

        return paths;
    }

    /**
     * A modified version of the standard BFS traversal, starting from the final
     * state that returns all the paths starting at a node.
     * E.g. from[3]: {{3, 6, 9}, {3, 4, 7, 6, 9}}
     *
     * @return all the paths starting at a node.
     */
    private List<Deque<Integer>>[] pathsFrom() {
        List<Deque<Integer>>[] from = initialisePaths(finalState);

        // Tracks whether nodes have been added to the FIFO
        boolean[] found = new boolean[numNodes];
        found[INITIAL] = true;

        Deque<Integer> fifo = new ArrayDeque<>();
        fifo.offer(finalState);
        found[finalState] = true;

        int child, parent;
        while (!fifo.isEmpty()) {
            child = fifo.poll();

            for (Edge e : incoming[child]) {
                parent = e.getSrc();

                for (Deque<Integer> path : from[child]) {
                    Deque<Integer> fromParent = new ArrayDeque<>(path);
                    fromParent.addFirst(parent);
                    from[parent].add(fromParent);
                }

                if (!found[parent]) {
                    fifo.offer(parent);
                    found[parent] = true;
                }
            }
        }

        return from;
    }

    /**
     * Completes all the paths.
     *
     * @param numPaths the number of paths to return.
     * @return complete paths.
     */
    public List<Deque<Integer>> getPaths(int numPaths) {
        List<Deque<Integer>>[] paths = pathsTo();
        List<Deque<Integer>>[] from = pathsFrom();

        int last;
        for (Deque<Integer> path : paths[INCOMPLETE]) {
            assert !path.isEmpty();
            last = path.pollLast();

            for (Deque<Integer> fromLast : from[last]) {
                Deque<Integer> cpy = new ArrayDeque<>(path);
                cpy.addAll(fromLast);
                paths[COMPLETE].add(cpy);
            }
        }

        // Removing duplicates: the user may ask for more paths than the total number of distinct
        // paths in the graph.
        return PathPruner.sample(paths[COMPLETE], numPaths).stream()
                .distinct()
                .toList();
    }

    /**
     * Returns an array of the edge transitions.
     *
     * @param path of nodes in the graph.
     * @return array of edge transitions.
     */
    public Edge[] getPathEdges(Deque<Integer> path) {
        Edge[] edges = new Edge[path.size() - 2]; // removing initial and final states

        path.pollLast();

        int i = 0;
        Iterator<Integer> it = path.iterator();

        int src, dst = -1;
        while (it.hasNext()) {
            src = i == 0 ? it.next() : dst;
            dst = it.next();
            edges[i++] = edgesById.get(src + "->" + dst);
        }

        return edges;
    }

    /**
     * Returns the initialisation of a structure that will store the different
     * paths up to a node.
     * e.g. paths[3]: { {0, 1, 3}, {0, 2, 4, 8, 3} }
     *
     * @param firstNode the first node to have a path.
     * @return upTo structure.
     */
    @SuppressWarnings("unchecked")
    private List<Deque<Integer>>[] initialisePaths(int firstNode) {
        List<Deque<Integer>>[] paths = new List[numNodes];

        for (int i = 0; i < numNodes; i++)
            paths[i] = new ArrayList<>(NUM_PATHS);

        Deque<Integer> initialPath = new ArrayDeque<>(PATH_LENGTH);
        initialPath.add(firstNode);
        paths[firstNode].add(initialPath);

        return paths;
    }

    // Graph construction

    /**
     * Initialises the outgoing, incoming, and states.
     */
    @SuppressWarnings("unchecked")
    private void initialiseStructures() {
        numNodes = nodesById.size() + 1;

        outgoing = new List[numNodes];
        for (int i = 0; i < numNodes; i++)
            outgoing[i] = new ArrayList<>(INITIAL_EDGES);

        incoming = new List[numNodes];
        for (int i = 0; i < numNodes; i++)
            incoming[i] = new ArrayList<>(INITIAL_EDGES);

        states = new State[numNodes];
        finalState = numNodes - 1;
        try {
            edgesById = new HashMap<>(numEdges);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * First pass through the DOT file.
     * Initialises graph and states. Populates the nodesById data structure.
     *
     * @param filePath DOT file path.
     * @throws FileNotFoundException when the DOT is not found.
     */
    private void countNodes(String filePath) throws IOException {
        numEdges = 0;
        BufferedReader buff = new BufferedReader(new FileReader(filePath));
        String line = buff.readLine();

        while (line != null) {
            if (isNodeDescription(line))
                nodesById.put(Long.parseLong(line.trim().split(SPACE)[0]), nodesById.size());
            else if (isEdgeDescription(line))
                numEdges++;
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
        String trimmedLine, line = buff.readLine();
        Edge edge;

        while (line != null) {
            trimmedLine = line.trim();
            if (isEdgeDescription(trimmedLine)) {
                String[] splitByEdge = trimmedLine.split(EDGE_CHAR);
                long src = Long.parseLong(splitByEdge[0].trim());
                long dst = Long.parseLong(splitByEdge[1].trim().split(SPACE)[0]);
                String labelField = trimmedLine.split(LABEL)[1].split(QUOTE)[1];
                String transition = labelField.split("\\(")[0];

                int srcId = nodesById.get(src);
                int dstId = nodesById.get(dst);
                String[] parameters = processParameters(labelField);
                edge = new Edge(srcId, dstId, transition, parameters);

                outgoing[srcId].add(edge);
                incoming[dstId].add(edge);

                edgesById.put(srcId + EDGE_CHAR.trim() + dstId, edge);

            } else if (isNodeDescription(line)) {
                State state = parser.parse(trimmedLine.split(QUOTE)[1]);
                long dotId = Long.parseLong(trimmedLine.split(SPACE)[0]);
                int nodeId = nodesById.get(dotId);
                states[nodeId] = state;

                // Adds an edge from all the final states to the super sink node.
                if (state.isFinalState()) {
                    edge = new Edge(nodeId, finalState, FINAL, new String[INITIAL_PARAMS]);
                    outgoing[nodeId].add(edge);
                    incoming[finalState].add(edge);
                }
            }

            line = buff.readLine();
        }

        buff.close();
    }

    /**
     * Process transition operation parameters
     *
     * @param label  edge label description (dot). E.g. label="postEnrollment(e1, t1, p1)"
     * @return an array of TLA model value IDs.
     */
    private String[] processParameters(String label) {
        // Split by "("; remove ")"; then split by ","
        return label.split("\\(")[1].replace(")", "").split(",");
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

    // Debugging - TODO remove

    /**
     * Returns a string representation of the graph's edges.
     *
     * @return edges by id.
     */
    public String edgesToString() {
        StringBuilder s = new StringBuilder();

        for (Map.Entry<String, Edge> e : edgesById.entrySet())
            s.append(e.getKey())
                    .append(": ")
                    .append(e.getValue().getTransition())
                    .append(Arrays.toString(e.getValue().getParameters()))
                    .append("\n");

        return s.toString();
    }

    /**
     * Returns a string representation of the given structure.
     *
     * @param paths collection.
     * @return representation of the given structure
     */
    public String pathsToString(List<Deque<Integer>> paths) {
        StringBuilder s = new StringBuilder("Number of paths: ");
        s.append(paths.size()).append("\n");

        for (Deque<Integer> path : paths) {
            s.append("{");

            for (Integer n : path)
                s.append(n).append(", ");

            s.delete(s.length() - 2, s.length());
            s.append("}\n");
        }

        return s.toString();
    }

    /**
     * Graph to string.
     *
     * @param in indicates whether to print incoming or outgoing edges.
     * @return string representation of the state space graph.
     */
    public String toString(boolean in) {
        StringBuilder s = in ? new StringBuilder("incoming: \n") : new StringBuilder("outgoing: \n");
        List<Edge>[] toPrint = in ? incoming : outgoing;

        for (int i = 0; i < toPrint.length; i++) {
            s.append(i).append(": {");

            for (Edge e : toPrint[i])
                if (in)
                    s.append(e.getSrc())
                            .append(" (").append(e.getTransition()).append(")")
                            .append("; ");
                else
                    s.append(e.getDst())
                            .append(" (").append(e.getTransition()).append(")")
                            .append("; ");

            if (!toPrint[i].isEmpty())
                s.delete(s.length() - 2, s.length());

            s.append("}\n");
        }

        return s.toString();
    }

    /**
     * Prints the graph's nodes correspondence.
     *
     * @return internal id : dot id
     */
    public String nodesToString() {
        StringBuilder s = new StringBuilder();

        for (Long id : nodesById.keySet())
            s.append(nodesById.get(id)).append(": ").append(id).append("\n");

        return s.toString();
    }

    /**
     * Prints the number of nodes and edges in the graph. Also prints the number of complete paths,
     * the average path size, largest and shortest path sizes of the path samples.
     *
     * @param fileName      dot file name.
     * @param paths        collection.
     * @param wanted       number of paths asked by the user.
     * @param elapsedTime  total time elapsed since the start of the program, in minutes.
     */
    public void printStats(String fileName, List<Deque<Integer>> paths, int wanted,
                           float elapsedTime) {
        System.out.println(STATS);
        System.out.printf("dot file name    :   %s\n", fileName);
        System.out.printf("nodes           :   %d\n", getNumNodes());
        System.out.printf("edges           :   %d\n", getNumEdges());
        System.out.printf("wanted paths    :   %d\n", wanted);
        System.out.printf("distinct paths  :   %d\n", paths.size());
        System.out.printf("avg size        :   %d\n", Math.round(PathPruner.averagePathSize(paths)));
        System.out.printf("max size        :   %d\n", PathPruner.largestPathSize(paths));
        System.out.printf("min size        :   %d\n", PathPruner.shortestPathSize(paths));
        System.out.printf("elapsed time    :   %.2f mins\n", elapsedTime);
        System.out.println(SPLIT);
    }

    /**
     * Prints graph data.
     */
    public void printGraph() {
        // Nodes by id
        System.out.println(NODES);
        System.out.println(nodesToString());

        // Edges by id
        System.out.println(EDGES);
        System.out.println(edgesToString());

        // Incoming and Outgoing structures
        System.out.println(GRAPH);
        System.out.println(toString(true));  // incoming
        System.out.println(toString(false)); // outgoing
    }

    public void printPaths(List<Deque<Integer>> paths) {
        System.out.println(PATHS);
        System.out.println(pathsToString(paths));
    }

}
