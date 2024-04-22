package graph.exceptions;

import java.io.Serial;

public class EdgeCapacityReachedException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public EdgeCapacityReachedException(String id) {
       super("Capacity reached for edge:" + id + ".\n");
    }
}
