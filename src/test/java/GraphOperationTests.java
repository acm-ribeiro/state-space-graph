import graph.StateSpaceGraph;
import graph.exceptions.NodeNotFoundException;
import graph.exceptions.UnableToReachSinkException;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class GraphOperationTests {

    private static final String TEST_FILE = "dot_files/small-graph-test.dot";
    private static final String SMALLEST_TEST_FILE = "dot_files/smallest_graph.dot";

    public static final long INITIAL_NODE_ID = 967637665389041036L;
    private static final Long SUPER_SINK_ID = 999999999999999999L;


    @Test
    @Disabled
    public void test_final_nodes() {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);

        // make StateSpaceGraph.getFinalStates() public to enable this test
        // assert(g.getFinalStates().size() == 2);
    }

    @Test
    @Disabled
    public void test_final_levels_init() {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);

        // make StateSpaceGraph.getLevels() public to enable this test

        //Map<Long, Integer> l = g.getLevels();
        boolean isOk = true;
//        for (int i : l.values())
//            if (i != -1) {
//                isOk = false;
//                break;
//            }

        assertTrue(isOk);
    }

    @Test
    public void test_super_sink_outgoing() throws NodeNotFoundException {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        long sink = g.getSuperSinkId();

        assert(g.getOutgoingEdges(sink).size() == 0);
    }

    @Test
    public void test_super_sink_incoming() throws NodeNotFoundException {
        StateSpaceGraph g = new StateSpaceGraph(TEST_FILE);
        long sink = g.getSuperSinkId();

        // we know this graph has two final states
        assert(g.getParents(sink).size() == 2);
    }

    @Test
    @Disabled
    public void test_level_graph() throws NodeNotFoundException, UnableToReachSinkException {
        StateSpaceGraph g = new StateSpaceGraph(SMALLEST_TEST_FILE);

        //g.dinic();
        //Map<Long, Integer> level = g.getLevelGraph(); // implement this to enable this test
        Map<Long, Integer> level = new HashMap<>();

        assert(level.get(-8639200975787036963L) == 0);
        assert(level.get(9211864019853093140L) == 1);
        assert(level.get(5449767392821248219L) == 1);
        assert(level.get(6745437152259851821L) == 2);
        assert(level.get(-1537023925787194541L) == 2);
        assert(level.get(7319127295324655972L) == 3);
        assert(level.get(4093726146083444784L) == 3);
        assert(level.get(96488363669597712L) == 2);
        assert(level.get(6752982062408681806L) == 2);
        assert(level.get(SUPER_SINK_ID) == -1);
    }

}
