package graph.exceptions;

import java.io.Serial;

public class UnableToReachSinkException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnableToReachSinkException() {
        super("Construction of the level graph failed: unable to reach the sink.");
    }
}
