package com.navigator.model;

import java.util.*;

public class Graph {
    public final List<Node> nodes;
    public final List<Edge> edges;
    private final Map<Integer,Integer> idToIndex = new HashMap<>();
    private final Map<String,Integer> nameToIndex = new HashMap<>();

    public Graph(List<Node> nodes, List<Edge> edges) {
        this.nodes = nodes;
        this.edges = edges;
        for (int i = 0; i < nodes.size(); i++) {
            idToIndex.put(nodes.get(i).id, i);
            nameToIndex.put(nodes.get(i).name, i);
        }
    }

    public int indexOf(int nodeId) {
        return idToIndex.getOrDefault(nodeId, -1);
    }

    public Node getNodeByName(String name) {
        Integer idx = nameToIndex.get(name);
        if (idx == null) return null;
        return nodes.get(idx);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

}