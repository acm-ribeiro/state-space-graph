import graph.StateSpaceGraph;
import graph.exceptions.EdgeCapacityReachedException;
import graph.exceptions.EdgeNotFoundException;
import graph.exceptions.VertexNotFoundException;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GraphOperationTests {

    private static final String TEST_FILE = "dot_files/small-graph-test.dot";
    public static final long INITIAL_VERTEX_ID = 967637665389041036L;

    @Test
    public void test_inc_flow() throws EdgeNotFoundException, EdgeCapacityReachedException, VertexNotFoundException {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        long tgt = g.getReachableVertices(INITIAL_VERTEX_ID).get(0);
        String edgeId = g.getEdgeId(INITIAL_VERTEX_ID, tgt);
        g.incEdgeFlow(edgeId, 1);

        assert (g.getEdge(edgeId).getFlow() == 1);
    }

    @Test
    public void test_inc_flow_edge_cap_exception() throws VertexNotFoundException {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        long tgt = g.getReachableVertices(INITIAL_VERTEX_ID).get(0);
        String edgeId = g.getEdgeId(INITIAL_VERTEX_ID, tgt);

        EdgeCapacityReachedException thrown = assertThrows (
                "",
                EdgeCapacityReachedException.class,
                () -> {g.incEdgeFlow(edgeId, 5);}
        );

        assertTrue(thrown.getMessage().contains(edgeId));
    }

    @Test
    public void test_inc_flow_no_vertex_exception() {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);

        VertexNotFoundException thrown = assertThrows (
                "",
                VertexNotFoundException.class,
                () -> {
                    long tgt = g.getReachableVertices(1L).get(0);
                    String edgeId = g.getEdgeId(INITIAL_VERTEX_ID, tgt);
                    g.incEdgeFlow(edgeId, 5);
                }
        );

        assertTrue(thrown.getMessage().contains("1"));
    }

    @Test
    @Disabled
    public void test_final_vertices() {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);

        // make StateSpaceGraph.getFinalStates() public to enable this test
        // assert(g.getFinalStates().size() == 2);
    }

    @Test
    public void test_super_sink_outgoing() throws VertexNotFoundException {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        long sink = g.getSuperSinkId();

        assert(g.getVertexOutgoingEdges(sink).size() == 0);
    }

    @Test
    public void test_super_sink_incoming() throws VertexNotFoundException {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        long sink = g.getSuperSinkId();

        // we know this graph has two final states
        assert(g.getParents(sink).size() == 2);
    }

}
