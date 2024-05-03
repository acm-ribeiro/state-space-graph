package graph;

import domain.State;

public class Vertex {

    private State state;
    private int level;

    public Vertex(State s) {
        state = s;
        level = -1;
    }

    /**
     * Returns the vertex state.
     *
     * @return state
     */
    public State getState() {
        return state;
    }

    /**
     * Returns the vertex leve.
     * @return level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the vertex level to the given value.
     * @param l new level.
     */
    public void setLevel (int l) {
        level = l;
    }

    /**
     * Checks whether this level was already visited.
     *
     * @return true if the vertex was visited; false otherwise.
     */
    public boolean visited () {
        return level != -1;
    }
}
