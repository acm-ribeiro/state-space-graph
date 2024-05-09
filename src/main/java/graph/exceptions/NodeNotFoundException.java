package graph.exceptions;

import java.io.Serial;

public class NodeNotFoundException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public NodeNotFoundException(long id) {
        super("Vertex not found:" + id + ".");
    }
}
