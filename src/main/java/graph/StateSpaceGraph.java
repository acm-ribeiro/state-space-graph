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

    // complete & incomplete paths
    private static final int PATH_CATEGORIES = 2;
    private static final int COMPLETE = 0;
    private static final int INCOMPLETE = 1;

    private static final int NUM_PATHS = 10;
    private static final int PATH_LENGTH = 10;

    // Initial state index
    private static int INITIAL = 0;

    private int finalState;           // final state index
    private int numNodes;             // number of nodes in the graph
    private List<Edge>[] outgoing;    // outgoing edges of all the graph's nodes
    private List<Edge>[] incoming;    // incoming edges of all the graph's nodes
    private State[] states;
    private Map<Long, Integer> nodesById;

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
     * Returns the transition label of originating a state.
     *
     * @param state destination state
     * @return transition label.
     */
    public String getStateTransition(int state) {
        return getState(state).getTransitionLabel();
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
    public List<Deque<Integer>>[] pathsTo() {
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
        while (!fifo.isEmpty()) {
            parent = fifo.poll();

            for (Edge e : outgoing[parent]) {
                child = e.getDst();
                Deque<Integer> upToChild = new ArrayDeque<>(upTo[parent]);
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
    public List<Deque<Integer>>[] pathsFrom() {
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
     * @return complete paths.
     */
    public List<Deque<Integer>> getPaths() {
        List<Deque<Integer>>[] paths = pathsTo();
        List<Deque<Integer>>[] from  = pathsFrom();

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

        return paths[COMPLETE];
    }

    /**
     * Returns an array of the path transitions.
     *
     * @param path of nodes in the graph.
     * @return array of path transitions.
     */
    public String[] getPathTransitions(Deque<Integer> path) {
        String[] transitions = new String[path.size()];

        int i = 0;
        while (!path.isEmpty() && i < transitions.length)
            transitions[i++] = getStateTransition(path.poll());

        return transitions;
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
                edge = new Edge(srcId, dstId, label);

                outgoing[srcId].add(edge);
                incoming[dstId].add(edge);

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
            if (outgoing[i].isEmpty()) {
                Edge edge = new Edge(i, finalState, FINAL);
                outgoing[i].add(edge);
                incoming[finalState].add(edge);
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


    // Debugging

    /**
     * Returns a string representation of the given structure.
     *
     * @param paths possible paths up to a node or from a node.
     * @return string representation of the paths.
     */
    public String pathsToString(String pathsName, List<Deque<Integer>>[] paths) {
        StringBuilder s = new StringBuilder(pathsName);
        s.append(": \n");

        for (int i = 0; i < paths.length; i++) {
            s.append("[").append(i).append("]: ");

            for (Deque<Integer> path : paths[i]) {
                if (path.isEmpty())
                    s.append("{}");
                else {
                    s.append("{");
                    for (Integer j : path)
                        s.append(j).append(", ");
                    s.delete(s.length() - 2, s.length());
                    s.append("}, ");
                    s.deleteCharAt(s.length() - 2);
                }
            }
            s.append("\n");
        }

        return s.toString();
    }

    /**
     * Returns a string representation of the given structure.
     *
     * @param complete complete paths.
     * @return representation of the given structure
     */
    public String completeToString(List<Deque<Integer>> complete) {
        StringBuilder s = new StringBuilder("[");
        s.append(complete.size()).append("]\n");

        for (Deque<Integer> path : complete) {
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
