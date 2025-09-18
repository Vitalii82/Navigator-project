package com.navigator.model;

public class Edge {
    public final int fromId;
    public final int toId;
    public final double weight;

    public Edge(int fromId, int toId, double weight) {
        this.fromId = fromId;
        this.toId = toId;
        this.weight = weight;
    }
}
