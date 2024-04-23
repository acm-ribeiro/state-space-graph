package graph;

import domain.State;
import graph.exceptions.EdgeCapacityReachedException;
import graph.exceptions.EdgeNotFoundException;
import graph.exceptions.VertexNotFoundException;
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
    private static final String NOT_FOUND = "File %s not found.\n";

    private static final long SUPER_SINK_ID = 999999999999999999L;

    private VisitorOrientedParser parser;

    private Map<Long, State> vertices;
    private Map<String, Edge> edges;
    private Map<Long, List<Long>> graph;
    private long initialState;

    public StateSpaceGraph (String fileName) {
        parser = new VisitorOrientedParser();
        vertices = new HashMap<>();
        edges = new HashMap<>();
        graph = new HashMap<>();
        populateVertices(fileName);
        populateEdges(fileName);
        addSuperSink();
    }

    /**
     * Checks whether the graph has the vertex with the given id.
     *
     * @param id vertex id.
     * @return true if the graph contains a vertex with the given id; false otherwise.
     */
    public boolean hasVertex(Long id) {
        return graph.containsKey(id) && vertices.containsKey(id);
    }

    /**
     * Returns the given vertex outgoing edges.
     *
     * @param src  vertex id.
     * @return a list of the vertex outgoing edges.
     * @throws VertexNotFoundException if the vertex src is not in the graph.
     */
    public List<Edge> getVertexOutgoingEdges(Long src) throws VertexNotFoundException {
        if(vertices.containsKey(src)) {
            List<Edge> outgoingEdges = new ArrayList<>();
            List<Long> adjacencyList = graph.get(src);

            if (adjacencyList.isEmpty())
                return outgoingEdges;

            String edgeId;

            for(Long tgt : adjacencyList) {
                edgeId = getEdgeId(src, tgt);
                outgoingEdges.add(edges.get(edgeId));
            }

            return outgoingEdges;
        } else
            throw new VertexNotFoundException(src);
    }

    /**
     * Returns a list of vertices reachable by the given vertex.
     *
     * @param src  vertex id.
     * @return a list of the reachable vertex ids.
     * @throws VertexNotFoundException if the vertex src is not in the graph.
     */
    public List<Long> getReachableVertices(long src) throws VertexNotFoundException {
        if(vertices.containsKey(src))
            return graph.get(src);
        else
            throw new VertexNotFoundException(src);
    }

    /**
     * Returns the vertex with the given id.
     *
     * @param id  vertex id.
     * @return vertex.
     * @throws VertexNotFoundException when the vertex is not in the graph.
     */
    public State getVertex(long id) throws VertexNotFoundException {
        if(vertices.containsKey(id))
            return vertices.get(id);
        else
            throw new VertexNotFoundException(id);
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
     * Checks whether the graph contains an edge from the vertex src to vertex tgt.
     *
     * @param src source vertex id
     * @param tgt target vertex id
     * @return true if there is an edge from src to tgt; false otherwise.
     * @pre hasVertex(src) && hasVertex(tgt)
     */
    public boolean hasEdge(Long src, Long tgt) {
        return graph.get(src).contains(tgt);
    }

    /**
     * Returns the number of vertices in the graph.
     *
     * @return number of vertices
     */
    public int getNumVertices() {
        return vertices.size();
    }

    /**
     * Increments the current flow of the edge with the given id, by the given value.
     *
     * @param id    edge id.
     * @param val   increment value.
     * @throws EdgeCapacityReachedException if the edge does not have enough capacity for the value to increment.
     * @throws EdgeNotFoundException if the graph does not have the edge with the given id.
     */
    public void incEdgeFlow(String id, int val) throws EdgeCapacityReachedException, EdgeNotFoundException {
        if (edges.containsKey(id)) {
            if (!edges.get(id).incFlow(val))
                throw new EdgeCapacityReachedException(id);
        } else
            throw new EdgeNotFoundException(id);

    }

    /**
     * Returns the edge id.
     *
     * @param src  edge source vertex id.
     * @param tgt  edge target vertex id.
     * @return edge id.
     */
    public String getEdgeId(long src, long tgt) {
        return src  + EDGE_CHAR + tgt;
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
     * Returns the graph's initial state id.
     *
     * @return initial state id.
     */
    public long getInitialState() {
        return initialState;
    }

    /**
     * Returns the super sink vertex id.
     *
     * @return super sink id.
     */
    public long getSuperSinkId() {
        return SUPER_SINK_ID;
    }

    /**
     * Returns all vertices that lead to the given target, directly.
     *
     * @param tgt target vertex id.
     * @return a list of vertex ids directly above the given vertex.
     * @throws VertexNotFoundException when tgt is not in the graph.
     */
    public List<Long> getParents(long tgt) throws VertexNotFoundException {
        if(graph.containsKey(tgt)) {
            List<Long> parents = new ArrayList<>();

            for(Entry<String, Edge> e : edges.entrySet())
                if (e.getValue().getTgt() == tgt)
                    parents.add(e.getValue().getSrc());

            return parents;
        } else
            throw new VertexNotFoundException(tgt);

    }



    /**
     * Populates the vertices data structure from the DOT file.
     *
     * @param file DOT file location.
     */
    private void populateVertices(String file) {
        try {
            Scanner sc = new Scanner(new File(file));
            String line;
            long id;
            int firstVertex = 0;

            while (sc.hasNext()) {
                line = sc.nextLine();
                if (isDescription(line) && !line.contains(EDGE_CHAR)) { // this line represents a vertex definition
                    id = processVertex(line);

                    // storing the initial state id
                    if (firstVertex == 0) {
                        initialState = id;
                        firstVertex++;
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
     * Checks whether a line from the DOT file is an edge or a vertex description.
     *
     * @param line line to check
     * @return true if the line is an edge or a vertex description.
     */
    private boolean isDescription(String line) {
        return line.contains(EDGE_CHAR) || line.contains(LABEL);
    }

    /**
     * Processes a line containing an edge description.
     *
     * @param edgeLine the line
     */
    private void processEdge(String edgeLine) {
        long src = Long.parseLong(edgeLine.split(EDGE_CHAR)[0]);
        long tgt = Long.parseLong(edgeLine.split(EDGE_CHAR)[1].trim().split(SPACE)[0]);
        String label = edgeLine.split(LABEL)[1].split(QUOTE)[1];

        if(!hasEdge(src, tgt))
            addEdge(src, tgt, label);
    }

    /**
     * Processes a line containing a vertex description.
     *
     * @param vertexLine the line
     * @return the vertex id; currently used to store the initial vertex.
     */
    private long processVertex(String vertexLine) {
        long id = Long.parseLong(vertexLine.split(SPACE)[0]);
        String stateStr = vertexLine.split(QUOTE)[1];
        addVertex(id, parser.parse(stateStr));

        return id;
    }

    /**
     * Adds a new vertex to the graph and the vertices' collection.
     *
     * @param id  vertex id
     * @param s   state
     * @pre !hasVertex(id)
     */
    private void addVertex(long id, State s) {
        if (!graph.containsKey(id)) {
            List<Long> adjList = new ArrayList<>();
            graph.put(id, adjList);
        }

        if (!vertices.containsKey(id))
            vertices.put(id, s);
    }

    /**
     * Adds a new labeled edge to the graph.
     *
     * @param src  edge source
     * @param tgt  edge target
     * @param label edge label
     * @pre !hasEdge(src, tgt)
     */
    private void addEdge(long src, long tgt, String label) {
        graph.get(src).add(tgt);
        String id = getEdgeId(src, tgt);
        edges.put(id, new Edge(src, tgt, label));
    }

    /**
     * Adds a super sink vertex to the graph.
     * A super sink is a vertex with no outgoing edges and all the incoming edges' sources are
     * from final state vertices.
     */
    private void addSuperSink() {
        // Finding all final states
        List<Long> finalStates = getFinalStates();

        if(!finalStates.isEmpty()) {
            // Adding the super sink and all edges from all the final states to the super sink.
            graph.put(SUPER_SINK_ID, new ArrayList<>());
            vertices.put(SUPER_SINK_ID, new State(null));
            List<Long> adjacencyList;

            for (Long finalState : finalStates) {
                adjacencyList = graph.get(finalState);
                adjacencyList.add(SUPER_SINK_ID);
                edges.put(getEdgeId(finalState, SUPER_SINK_ID), new Edge(finalState, SUPER_SINK_ID, "sink"));
            }
        }
    }

    /**
     * Finds the graph's final states. A final state is a state with the element F set to true.
     * The only vertex with no outgoing edges is the super sink, and this is not a valid final state.
     *
     * @return final states vertex ids.
     */
    private List<Long> getFinalStates() { // TODO make private
        List<Long> finalStates = new ArrayList<>();
        Long id;

        for(Entry<Long, List<Long>> entry : graph.entrySet()) {
            id = entry.getKey();
            if (!isSink(id) && vertices.get(id).isFinalState())
                finalStates.add(entry.getKey());
        }

        return finalStates;
    }

    /**
     * Checks whether the provided id is the sink id.
     *
     * @param id vertex id
     * @return true if the given id is the sink id; false otherwise.
     */
    private boolean isSink(long id) {
        return id == SUPER_SINK_ID;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        List<Long> adjList;

        for (Map.Entry<Long, List<Long>> e : graph.entrySet()) {
            s.append(e.getKey());
            s.append("\n  [");

            int counter = 0;
            adjList = e.getValue();
            for (Long v : adjList)
                if (counter == 0) {
                    s.append(v);
                    counter++;
                } else {
                    s.append(", ");
                    s.append(v);
                }
            s.append("]\n");
        }

        return s.toString();
    }
}
