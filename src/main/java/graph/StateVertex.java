package graph;

public class StateVertex {

    private int id;
    private String state;

    public StateVertex() {

    }

    public StateVertex (int id, String state) {
        this.id = id;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public void setState (String state) {
        this.state = state;
    }
}
