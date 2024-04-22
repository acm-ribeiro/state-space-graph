import graph.StateSpaceGraph;
import graph.exceptions.VertexNotFoundException;
import org.junit.Test;

import java.util.List;

public class ReadGraphTests {

    private static final String TEST_FILE = "dot_files/small-graph-test.dot";
    private static final long INITIAL_VERTEX_ID = 967637665389041036L;

    @Test
    public void test_graph_vertex_read() {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        assert (g.getNumVertices() == 25);
    }

    @Test
    public void test_graph_edge_read() {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        assert (g.getNumEdges() == 66);
    }

    @Test
    public void initial_state_edges () throws VertexNotFoundException {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        assert(g.hasVertex(INITIAL_VERTEX_ID));

        List<Long> adjacencyList = g.getReachableVertexes(INITIAL_VERTEX_ID);
        assert(adjacencyList.size() == 2);
        assert(adjacencyList.contains(8756997901130288749L));
        assert(adjacencyList.contains(-8814944471661943105L));
    }

    @Test
    public void initial_state() {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        assert(g.getInitialState() == INITIAL_VERTEX_ID);
    }

}
