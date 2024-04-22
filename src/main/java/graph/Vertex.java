package graph;

import domain.Entity;
import domain.State;
import domain.StateElement;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Vertex {
    private int id;
    private State state;

    public Vertex (int id, State state) {
        this.id = id;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public State getState() {
        return state;
    }

    public void stateTest() {
        List<StateElement> elems = state.getElements();

        for (StateElement elem : elems) {
            // entities: p, t, pc, f, req, res
             Map<String, Entity> entities = elem.getEntities();
             for (Entry<String, Entity> entry : entities.entrySet()) {
                 // TODO the class entity of the parser needs to check if this is a state entity
                 // and if it's empty
             }
        }
    }
}
