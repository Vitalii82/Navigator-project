package com.navigator.service.intarfaces;

import com.navigator.model.Node;
import com.navigator.model.Edge;
import com.navigator.service.PathResult;

import java.util.List;

public interface IGraphService {
    List<Node> getAllNodes();
    List<Edge> getAllEdges();
    PathResult shortestPath(String fromName, String toName);
    void reloadGraph();

    boolean addNode(String name, double x, double y);
    boolean addEdge(String fromName, String toName, double weight);
    boolean removeNode(String name);
    boolean removeEdge(String fromName, String toName);
}
