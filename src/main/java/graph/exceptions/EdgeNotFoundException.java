package graph.exceptions;

import java.io.Serial;

public class EdgeNotFoundException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public EdgeNotFoundException(String id) {
        super("Edge not found:" + id + ".");
    }
}
