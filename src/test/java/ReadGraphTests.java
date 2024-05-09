import graph.StateSpaceGraph;
import graph.exceptions.NodeNotFoundException;
import org.junit.Test;

import java.util.List;

public class ReadGraphTests {

    private static final String TEST_FILE = "dot_files/small-graph-test.dot";
    private static final long INITIAL_VERTEX_ID = 967637665389041036L;

    @Test
    public void test_graph_vertex_read() {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        assert (g.getNumNodes() == 26); // 25 without the super sink
    }

    @Test
    public void test_graph_edge_read() {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        assert (g.getNumEdges() == 68);
    }

    @Test
    public void initial_state_edges () throws NodeNotFoundException {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        assert(g.hasNode(INITIAL_VERTEX_ID));

        List<Long> adjacencyList = g.getReachableNodes(INITIAL_VERTEX_ID);
        assert(adjacencyList.size() == 2);
        assert(adjacencyList.contains(8756997901130288749L));
        assert(adjacencyList.contains(-8814944471661943105L));
    }

    @Test
    public void initial_state() {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        assert(g.getSource() == INITIAL_VERTEX_ID);
    }

}
