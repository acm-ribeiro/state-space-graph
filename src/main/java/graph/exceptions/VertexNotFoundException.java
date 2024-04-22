package graph.exceptions;

import java.io.Serial;

public class VertexNotFoundException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public VertexNotFoundException(long id) {
        super("Vertex not found:" + id + ".");
    }
}
