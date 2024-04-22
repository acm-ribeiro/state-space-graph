import graph.StateSpaceGraph;
import graph.exceptions.EdgeCapacityReachedException;
import graph.exceptions.EdgeNotFoundException;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GraphOperationTests {

    private static final String TEST_FILE = "dot_files/small-graph-test.dot";
    public static final long INITIAL_VERTEX_ID = 967637665389041036L;

    @Test
    public void test_inc_flow() throws EdgeNotFoundException, EdgeCapacityReachedException {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        long tgt = g.getVertexOutgoingEdges(INITIAL_VERTEX_ID).get(0);
        String edgeId = g.getEdgeId(INITIAL_VERTEX_ID, tgt);
        g.incEdgeFlow(edgeId, 1);

        assert (g.getEdge(edgeId).getFlow() == 1);
    }

    @Test
    public void test_inc_flow_exception() throws EdgeNotFoundException, EdgeCapacityReachedException {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        long tgt = g.getVertexOutgoingEdges(INITIAL_VERTEX_ID).get(0);
        String edgeId = g.getEdgeId(INITIAL_VERTEX_ID, tgt);

        EdgeCapacityReachedException thrown = assertThrows (
                "",
                EdgeCapacityReachedException.class,
                () -> {g.incEdgeFlow(edgeId, 5);}
        );

        assertTrue(thrown.getMessage().contains(edgeId));

    }

}
