package graph;

import domain.State;
import parser.VisitorOrientedParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.Map.Entry;

public class SSG {

    private static final String EDGE_CHAR = " -> ";
    private static final String LABEL = "label=";
    private static final String SPACE = " ";
    private static final String QUOTE = "\"";
    private static final String NOT_FOUND = "File %s not found.\n";

    private List<SSGEdge>[] graph;
    private State[] states;
    private Map<Long, Integer> nodesById;


    public SSG (String filePath) {
        nodesById = new HashMap<>();
        initialiseGraph(Objects.requireNonNull(initialProcessing(filePath)));
        initialiseStates(filePath);

    }

    /**
     * Initialises the states array.
     *
     * @param filePath DOT file path.
     */
    private void initialiseStates(String filePath) throws FileNotFoundException {
        states = new State[graph.length];
        VisitorOrientedParser parser = new VisitorOrientedParser();
        Scanner sc = new Scanner(new FileReader(filePath));
        String line;

        while (sc.hasNextLine()) {
            line = sc.nextLine();

            if (isNodeLine(line)) {
                State state = parser.parse(line.split(QUOTE)[1]);
                long id = Long.parseLong(line.split(SPACE)[0]);
                int nodeId = nodesById.get(id);
                states[nodeId] = state;
            }
        }

        sc.close();
    }

    /**
     * Initial DOT file processing. Removes duplicate edge definitions and finds the number of edges and nodes
     * of the SSg.
     *
     * @param filePath DOT file path.
     * @return edges by id.
     * 
     */
    private Map<String, SSGEdge> initialProcessing(String filePath) throws FileNotFoundException {

        Scanner sc = new Scanner(new FileReader(filePath));
        Map<String, SSGEdge> edges = new HashMap<>();
        String line;

        while (sc.hasNextLine()) {
            line = sc.nextLine();

            if (isNodeLine(line)) {
                long id = Long.parseLong(line.split(SPACE)[0]);
                nodesById.put(id, nodesById.size());
            } else if (isEdgeLine(line)) {
                long src = Long.parseLong(line.split(EDGE_CHAR)[0]);
                long dst = Long.parseLong(line.split(EDGE_CHAR)[1].trim().split(SPACE)[0]);

                int srcId = nodesById.get(src);
                int dstId = nodesById.get(dst);

                String label = line.split(LABEL)[1].split(QUOTE)[1];
                String edgeId = getEdgeId(srcId, dstId);

                if (!edges.containsKey(edgeId))
                    edges.put(edgeId, new SSGEdge(srcId, dstId, label));
            }
        }
        sc.close();

        return edges;

    }

    /**
     * Initialises the state space graph.
     *
     * @param edgesById map of the graph's edges by id.
     */
    private void initialiseGraph(Map<String, SSGEdge> edgesById) {
        graph = new List[nodesById.size() + 1]; // +1 for super sink

        for (SSGEdge e : edgesById.values()) {
            int srcIdx = e.getSrc();

            if(graph[srcIdx] == null)
                graph[srcIdx] = new ArrayList<>();

            graph[srcIdx].add(e);
        }
    }

    /**
     * Returns a string representation of an edge id.
     *
     * @param src   source node id
     * @param dst   target node id
     * @return edge id.
     */
    private String getEdgeId(int src, int dst) {
        return src + EDGE_CHAR + dst;
    }

    /**
     * Checks whether a DOT file line corresponds to a node description.
     *
     * @param line DOT file line.
     * @return true if the line is a node description; false otherwise.
     */
    private boolean isNodeLine(String line) {
        return line.contains(LABEL) && !line.contains(EDGE_CHAR);
    }

    /**
     * Checks whether a DOT file line corresponds to an edge description.
     *
     * @param line DOT file line.
     * @return true if the line is an edge description; false otherwise.
     */
    private boolean isEdgeLine(String line) {
        return line.contains(EDGE_CHAR);
    }
}
