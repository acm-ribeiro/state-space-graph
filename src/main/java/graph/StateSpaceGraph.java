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

    private VisitorOrientedParser parser;

    private Map<Long, State> vertexes;
    private Map<String, Edge> edges;
    private Map<Long, List<Long>> graph;


    public StateSpaceGraph(String fileName) {
        parser = new VisitorOrientedParser();
        vertexes = new HashMap<>();
        edges = new HashMap<>();
        graph = new HashMap<>();
        populateVertices(fileName);
        populateEdges(fileName);
    }

    /**
     * Checks whether the graph has the vertex with the given id.
     *
     * @param id vertex id.
     * @return true if the graph contains a vertex with the given id; false otherwise.
     */
    public boolean hasVertex(Long id) {
        return graph.containsKey(id) && vertexes.containsKey(id);
    }

    public List<Long> getVertexOutgoingEdges(Long id) {
        return graph.get(id);
    }

    /**
     * Returns the vertex with the given id.
     *
     * @param id  vertex id.
     * @return vertex.
     * @throws VertexNotFoundException when the vertex is not in the graph.
     */
    public State getVertex(long id) throws VertexNotFoundException {
        if(vertexes.containsKey(id))
            return vertexes.get(id);
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
        return vertexes.size();
    }

    /**
     * Increments the current flow of the edge with the given id, with the given value.
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
     * Populates the vertices data structure from the DOT file.
     *
     * @param file DOT file location.
     */
    private void populateVertices(String file) {
        try {
            Scanner sc = new Scanner(new File(file));
            String line;

            while (sc.hasNext()) {
                line = sc.nextLine();
                if (isDescription(line) && !line.contains(EDGE_CHAR)) // this line represents a vertex definition
                    processVertex(line);
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

    private void processEdge(String edgeLine) {
        long src = Long.parseLong(edgeLine.split(EDGE_CHAR)[0]);
        long tgt = Long.parseLong(edgeLine.split(EDGE_CHAR)[1].trim().split(SPACE)[0]);
        String label = edgeLine.split(LABEL)[1].split(QUOTE)[1];

        if(!hasEdge(src, tgt))
            addEdge(src, tgt, label);
    }

    private void processVertex(String vertexLine) {
        //System.out.println(vertexLine);
        long id = Long.parseLong(vertexLine.split(SPACE)[0]);
        String stateStr = vertexLine.split(QUOTE)[1];

        addVertex(id, parser.parse(stateStr));
    }

    private void addVertex(long id, State s) {
        if (!graph.containsKey(id)) {
            List<Long> adjList = new ArrayList<>();
            graph.put(id, adjList);
        }

        if (!vertexes.containsKey(id))
            vertexes.put(id, s);
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
