package graph;

import domain.State;
import graph.exceptions.EdgeNotFoundException;
import graph.exceptions.NodeNotFoundException;
import parser.VisitorOrientedParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Map.Entry;


public class StateSpaceGraph {

    private static final String EDGE_CHAR = " -> ";
    private static final String LABEL = "label=";
    private static final String QUOTE = "\"";
    private static final String SPACE = " ";
    private static final String SINK = "sink";
    private static final String NOT_FOUND = "File %s not found.\n";
    private static final String RESIDUAL = "_r";

    // Super sink node id
    private static final long SUPER_SINK_ID = 999999999999999999L;

    // Flow value for the initial step of Dinic's algorithm. Divided by 2 to avoid overflows.
    private static final int INF = Integer.MAX_VALUE / 2;

    // Initial edge capacity
    private static final int INITIAL_CAPACITY  = 1;
    private static final int RESIDUAL_CAPACITY = 0;

    private VisitorOrientedParser parser;   // state parser 

    private Map<Long, State> nodes;         // nodes by id
    private Map<String, Edge> edges;        // edges by id
    private Map<Long, Integer> level;       // level graph

    private Map<Long, List<Edge>> graph;

    private long source;                    // source node id

    public StateSpaceGraph(String fileName) {
        parser = new VisitorOrientedParser();
        nodes = new HashMap<>();
        edges = new HashMap<>();
        graph = new HashMap<>();
        populateNodes(fileName);
        populateEdges(fileName);
        addSuperSink();
    }

    /**
     * Checks whether the graph has the node with the given id.
     *
     * @param id node id.
     * @return true if the graph contains a node with the given id; false otherwise.
     */
    public boolean hasNode(Long id) {
        return graph.containsKey(id) && nodes.containsKey(id);
    }

    /**
     * Returns the given node's outgoing edges.
     *
     * @param src node id.
     * @return a list of the node outgoing edges.
     * @throws NodeNotFoundException if the node src is not in the graph.
     */
    public List<Edge> getOutgoingEdges(Long src) throws NodeNotFoundException {
        if (nodes.containsKey(src)) {
            List<Edge> outgoingEdges = new ArrayList<>();
            List<Edge> adjacencyList = graph.get(src);

            if (adjacencyList.isEmpty())
                return outgoingEdges;

            String edgeId;

            for (Edge edge : adjacencyList) {
                edgeId = edge.getId();
                outgoingEdges.add(edges.get(edgeId));
            }

            return outgoingEdges;
        } else
            throw new NodeNotFoundException(src);
    }

    /**
     * Returns the node with the given id.
     *
     * @param id node id.
     * @return node.
     * @throws NodeNotFoundException when the node is not in the graph.
     */
    public State getNode(Long id) throws NodeNotFoundException {
        if (nodes.containsKey(id))
            return nodes.get(id);
        else
            throw new NodeNotFoundException(id);
    }

    /**
     * Returns the edge with the given id.
     *
     * @param id edge's id
     * @return edge
     * @throws EdgeNotFoundException when the edge is not in the graph.
     */
    public Edge getEdge(String id) throws EdgeNotFoundException {
        if (edges.containsKey(id))
            return edges.get(id);
        else
            throw new EdgeNotFoundException(id);
    }

    /**
     * Checks whether the graph contains an edge with the given ID.
     *
     * @param id edge ID.
     * @return true if there is an edge with the given ID; false otherwise.
     */
    public boolean hasEdge(String id) {
        return edges.containsKey(id);
    }

    /**
     * Returns the number of nodes in the graph.
     *
     * @return number of nodes
     */
    public int getNumNodes() {
        return nodes.size();
    }

    /**
     * Returns the edge id.
     *
     * @param src edge source node id.
     * @param tgt edge target node id.
     * @return edge id.
     */
    public String getEdgeId(long src, long tgt) {
        return src + EDGE_CHAR + tgt;
    }

    /**
     * Returns the number of edges in the graph, including self-loops.
     *
     * @return number of edges
     */
    public int getNumEdges() {
        return edges.size();
    }

    /**
     * Returns the graph's initial node id.
     *
     * @return initial node id.
     */
    public long getSource() {
        return source;
    }

    /**
     * Returns the super sink node id.
     *
     * @return super sink id.
     */
    public long getSuperSinkId() {
        return SUPER_SINK_ID;
    }

    /**
     * Returns all nodes that lead to the given target, directly.
     *
     * @param tgt target node id.
     * @return a list of node ids directly above the given node.
     * @throws NodeNotFoundException when tgt is not in the graph.
     */
    public List<Long> getParents(long tgt) throws NodeNotFoundException {
        if (graph.containsKey(tgt)) {
            List<Long> parents = new ArrayList<>();

            for (Entry<String, Edge> e : edges.entrySet())
                if (e.getValue().getTgt() == tgt)
                    parents.add(e.getValue().getSrc());

            return parents;
        } else
            throw new NodeNotFoundException(tgt);

    }

    /**
     * Dinic's algorithm implementation.
     *
     * @return maximum flow value.
     * @throws NodeNotFoundException when for some reason we reach an invalid node.
     */
    public int dinic() throws NodeNotFoundException {
        /*
         * Shimon Even and Alon Itai's optimisation for pruning dead ends.
         * This map tracks which edge we should take next for each node, i.e., next.get(n) indicates the
         * next edge index to take in the adjacency list for node n.
         */
        Map<Long, Integer> next;
        int maxFlow = 0;

        while (bfs()) {
            // next is reset in each iteration to allow taking previously forbidden edges
            next = initialiseNodeMap(0);
            System.out.println(integerMapToString(next, "Next Map (init)"));
            int f = dfs(source, next, INF);

            while (f != 0) {
                maxFlow += f;
                f = dfs(source, next, INF);
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
        // Initialises the level graph with all values at -1
        level = initialiseNodeMap(-1);

        // Stores the nodes to visit next
        Deque<Long> q = new ArrayDeque<>(getNumNodes());

        // Add source node
        q.add(source);
        level.put(source, 0);

        while (!q.isEmpty()) {
            long n = q.poll(); // dequeue
            List<Edge> outgoing = getOutgoingEdges(n);

            for (Edge e : outgoing) {
                int cap = e.getRemainingCapacity();
                long tgt = e.getTgt();

                if (cap > 0 && level.get(tgt) == -1) {
                    level.put(tgt, level.get(n) + 1);
                    q.offer(tgt); // enqueue
                }
            }
        }

        // Return whether we were able to reach the super sink
        return level.get(SUPER_SINK_ID) != -1;
    }

    /**
     * Recursive depth-first search.
     *
     * @param current the current node
     * @param next    a map indicating which edge to take next for each node
     * @param flow    the minimum flow value along the path so far (starts at positive infinity)
     * @return maximum flow along the path.
     */
    private int dfs(long current, Map<Long, Integer> next, int flow) {
        // if we reached the sink, the algorithm should terminate
        if (current == SUPER_SINK_ID)
            return flow;

        // number of outgoing edges of the current node
        int numEdges = graph.get(current).size();
        int edgeIdx = next.get(current);

        while (edgeIdx < numEdges) {
            Edge edge = graph.get(current).get(edgeIdx);
            int capacity = edge.getRemainingCapacity();

            if (capacity > 0 && level.get(edge.getTgt()) == level.get(current) + 1) {
                int bottleneck = dfs(edge.getTgt(), next, Math.min(flow, capacity));

                if (bottleneck > 0) {
                    augment(edge, bottleneck);
                    return bottleneck;
                }
            }

            // moving forward
            edgeIdx++;
            next.put(current, edgeIdx);
        }

        return 0;
    }

    /**
     * Returns all the outgoing edges of the node with the given id.
     *
     * @param src node id.
     * @return list of all outgoing edges.
     * @throws NodeNotFoundException when src is not in the graph.
     */
    private List<Edge> getOutgoingEdges(long src) throws NodeNotFoundException {
        if (!nodes.containsKey(src))
            throw new NodeNotFoundException(src);

        return graph.get(src);
    }

    /**
     * Populates the nodes data structure from the DOT file.
     *
     * @param file DOT file location.
     */
    private void populateNodes(String file) {
        try {
            Scanner sc = new Scanner(new File(file));
            String line;
            long id;
            int firstNode = 0;

            while (sc.hasNext()) {
                line = sc.nextLine();
                if (isDescription(line) && !line.contains(EDGE_CHAR)) { // this line represents a node definition
                    id = processNode(line);

                    // storing the initial node id
                    if (firstNode == 0) {
                        source = id;
                        firstNode++;
                    }
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.err.printf(NOT_FOUND, file);
        }

    }

    /**
     * Populates the edges data structure from the DOT file.
     *
     * @param file DOT file location
     */
    private void populateEdges(String file) {
        try {
            Scanner sc = new Scanner(new File(file));
            String line;
            while (sc.hasNext()) {
                line = sc.nextLine();
                if (isDescription(line) && line.contains(EDGE_CHAR)) // this line represents an edge definition
                    processEdge(line);
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.err.printf(NOT_FOUND, file);
        }
    }

    /**
     * Checks whether a line from the DOT file is an edge or a node description.
     *
     * @param line line to check
     * @return true if the line is an edge or a node description.
     */
    private boolean isDescription(String line) {
        return line.contains(EDGE_CHAR) || line.contains(LABEL);
    }

    /**
     * Processes a line containing an edge description.
     *
     * @param edgeLine the line
     */
    private void processEdge (String edgeLine) {
        long src = Long.parseLong(edgeLine.split(EDGE_CHAR)[0]);
        long tgt = Long.parseLong(edgeLine.split(EDGE_CHAR)[1].trim().split(SPACE)[0]);
        String label = edgeLine.split(LABEL)[1].split(QUOTE)[1];
        String edgeId = getEdgeId(src, tgt);

        if (!hasEdge(edgeId))
            addEdge(src, tgt, label);
    }

    /**
     * Processes a line containing a node description.
     *
     * @param nodeLine the line
     * @return the node id; currently used to store the initial node.
     */
    private long processNode(String nodeLine) {
        long id = Long.parseLong(nodeLine.split(SPACE)[0]);
        String stateStr = nodeLine.split(QUOTE)[1];
        addNode(id, parser.parse(stateStr));

        return id;
    }

    /**
     * Adds a new node to the graph and the nodes' collection.
     *
     * @param id node id
     * @param s  state
     * @pre !hasNode(id)
     */
    private void addNode(long id, State s) {
        if (!graph.containsKey(id)) {
            List<Edge> adjList = new ArrayList<>();
            graph.put(id, adjList);
        }

        if (!nodes.containsKey(id))
            nodes.put(id, s);
    }

    /**
     * Adds a new labeled edge to the graph.
     *
     * @param src   edge source
     * @param tgt   edge target
     * @param label edge label
     * @pre !hasEdge(src, tgt)
     */
    private void addEdge(long src, long tgt, String label) {
        // Creating the new edge
        String edgeId = getEdgeId(src, tgt);
        Edge edge = new Edge(edgeId, src, tgt, label, INITIAL_CAPACITY);

        // Creating the complimentary residual edge
        String residualId = getResidualEdgeId(tgt, src);
        Edge residual = new Edge(residualId, tgt, src, getResidualLabel(src, label), RESIDUAL_CAPACITY);
        edge.setResidual(residualId);
        residual.setResidual(edgeId);

        // Adding both edges to the edges collection
        edges.put(edgeId, edge);
        edges.put(residualId, residual);

        // Adding both edges to the graph
        graph.get(src).add(edge);
        graph.get(tgt).add(residual);
    }

    /**
     * Adds a super sink node to the graph.
     * A super sink is a node with no outgoing edges and all the incoming edges' sources are
     * from final state nodes.
     */
    private void addSuperSink() {
        // Finding all final states
        List<Long> finalStates = getFinalStates();

        if (!finalStates.isEmpty()) {
            // Adding the super sink and all edges from all the final states to the super sink.
            graph.put(SUPER_SINK_ID, new ArrayList<>());
            nodes.put(SUPER_SINK_ID, new State(null));
            List<Edge> finalAdjList, sinkAdjList;
            String edgeId, residualId, residualLabel;

            for (Long finalState : finalStates) {
                // Creating the edge
                edgeId = getEdgeId(finalState, SUPER_SINK_ID);
                Edge edge = new Edge(edgeId, finalState, SUPER_SINK_ID, SINK, INF);

                // Creating the residual edge
                residualId = getResidualEdgeId(SUPER_SINK_ID, finalState);
                residualLabel = getResidualLabel(finalState, SINK);
                Edge residual = new Edge(residualId, SUPER_SINK_ID, finalState, residualLabel, RESIDUAL_CAPACITY);

                // Setting up residuals
                edge.setResidual(residualId);
                residual.setResidual(edgeId);

                // Adding both edges to the edges' collection
                edges.put(edgeId, edge);
                edges.put(residualId, residual);

                // Adding the edge from the final state to the super sink
                finalAdjList = graph.get(finalState);
                finalAdjList.add(edge);

                // Adding residual edges from the super sink to the final states
                sinkAdjList = graph.get(SUPER_SINK_ID);
                sinkAdjList.add(residual);
            }
        }
    }

    /**
     * Finds the graph's final states. A final state is a state with the element F set to true.
     * The only node with no outgoing edges is the super sink, and this is not a valid final state.
     *
     * @return final states node ids.
     */
    private List<Long> getFinalStates() {
        List<Long> finalStates = new ArrayList<>();
        Long id;

        for (Entry<Long, List<Edge>> entry : graph.entrySet()) {
            id = entry.getKey();
            if (!isSink(id) && nodes.get(id).isFinalState())
                finalStates.add(entry.getKey());
        }

        return finalStates;
    }

    /**
     * Checks whether the provided id is the sink id.
     *
     * @param id node id
     * @return true if the given id is the sink id; false otherwise.
     */
    private boolean isSink(long id) {
        return id == SUPER_SINK_ID;
    }

    /**
     * Initializes a map with key-value pairs (nodeId, value), where nodeId are all node's IDs and value is given.
     *
     * @param value value of all entries of the map.
     * @return a map of nodeIds, value.
     */
    private Map<Long, Integer> initialiseNodeMap(int value) {
        Map<Long, Integer> map = new HashMap<>(getNumNodes());

        for (long nodeId : nodes.keySet())
            map.put(nodeId, value);

        return map;
    }

    /**
     * Augments the flow of the given edge by the given bottleneck value.
     *
     * @param edge          edge to increment the flow.
     * @param bottleneck    value to increment to the edge's flow.
     */
    private void augment(Edge edge, int bottleneck) {
        Edge residual = edges.get(edge.getResidualId());
        edge.incFlow(bottleneck);
        residual.decFlow(bottleneck);
    }

    /**
     * Builds the label for a residual edge. The label is composed of the original label, the residual tag, and the
     * first three digits of the target node's id.
     *
     * @param tgt               target node id
     * @param originalLabel     original edge label
     * @return residual label
     */
    private String getResidualLabel(long tgt, String originalLabel) {
        String residualLabel = originalLabel + RESIDUAL + " (";

        if (tgt < 0)
            residualLabel += String.valueOf(tgt).substring(0, 4) + ")";
        else
            residualLabel += String.valueOf(tgt).substring(0, 3) + ")";

        return residualLabel;
    }

    /**
     * Returns the residual edge ID.
     * This ID is made up of the source and targes nodes' IDs and the original edge's label.
     *
     * @param src source node ID.
     * @param tgt target node ID.
     * @return residual edge ID.
     */
    private String getResidualEdgeId(long src, long tgt) {
        return getEdgeId(src, tgt) + RESIDUAL;
    }

    /**
     * Returns a textual representation of the given map structure.
     *
     * @return string representing the map structure; or the empty string if map is null.
     */
    public String integerMapToString(Map<Long, Integer> map, String name) {
        if (level != null) {
            StringBuilder s = new StringBuilder();
            s.append("\n------------- "+ name + " -------------\n");

            for (Entry<Long, Integer> e : map.entrySet()) {
                s.append(e.getKey());
                s.append(" = ");
                s.append(e.getValue());
                s.append("\n");
            }

            s.append("---------------------------------------\n");
            return s.toString();
        }

        return "";
    }

    /**
     * Returns a compact textual representation of the graph's edges.
     * The nodes' IDs are shortened for readability purposes.
     *
     * @return string representing the graph's edges.
     */
    public String edgesToStringCompact() {
        StringBuilder s = new StringBuilder();
        s.append("------------------------- Edges (");
        s.append(getNumEdges());
        s.append(") -------------------------\n");

        for(Entry<String, Edge> e: edges.entrySet()) {
            Edge edge = e.getValue();
            long src = edge.getSrc();
            long tgt = edge.getTgt();
            String srcStr = src < 0 ? String.valueOf(src).substring(0, 4) : String.valueOf(src).substring(0, 3);
            String tgtStr = tgt < 0 ? String.valueOf(tgt).substring(0, 4) : String.valueOf(tgt).substring(0, 3);

            s.append(srcStr).append(EDGE_CHAR).append(tgtStr);

            if (src > 0 && tgt > 0)
                s.append("   :  ");
            else if(src > 0 || tgt > 0)
                s.append("  :  ");

            s.append(edge.getLabel()).append("\n");
        }

        s.append("---------------------------------------------------------------");

        return s.toString();
    }

    /**
     * Returns a textual representation of the graph's edges.
     *
     * @return string representing the graph's edges.
     */
    public String edgesToString() {
        StringBuilder s = new StringBuilder();
        s.append("------------------------- Edges (");
        s.append(getNumEdges());
        s.append(") -------------------------\n");

        for (Entry<String, Edge> e: edges.entrySet())
            s.append(e.getKey()).append("     ").append(e.getValue().getLabel()).append("\n");

        s.append("---------------------------------------------------------------");

        return s.toString();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("---------------------- State Space Graph ----------------------\n");

        for (Entry<Long, List<Edge>> e : graph.entrySet()) {
            long src = e.getKey();
            List<Edge> adj = e.getValue();

            s.append(src);
            s.append(": ");

            for (Edge edge : adj) {
                String edgeId = edge.getId();
                String residualId = getResidualEdgeId(src, edge.getTgt());

                if (edges.containsKey(edgeId))
                    s.append(edges.get(edgeId).getLabel());
                else if (edges.containsKey(residualId))
                    s.append(edges.get(residualId).getLabel());

                s.append(", ");
            }

            // removing last space and comma
            if(!adj.isEmpty()) {
                s.deleteCharAt(s.length() - 1);
                s.deleteCharAt(s.length() - 1);
            }
            s.append("\n");
        }
        s.append("\n");
        return s.toString();
    }

}
