package org.example;

import org.jgrapht.graph.DefaultEdge;

public class LabeledEdge extends DefaultEdge {

    private String label;

    public LabeledEdge(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return getSource() + " -> " + getTarget() + " : " + label;
    }
}
