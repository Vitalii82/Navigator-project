package com.navigator.service;

import com.navigator.db.Database;
import com.navigator.math.FloydWarshall;
import com.navigator.model.Graph;
import com.navigator.model.Node;

import java.util.List;

public class NavigatorService implements AutoCloseable {
    private final Database db;
    private final Graph graph;
    private final FloydWarshall fw;

    public static class PathResult {
        public final double distance;
        public final List<String> path;
        public PathResult(double d, List<String> p) { this.distance = d; this.path = p; }
    }

    public NavigatorService() {
        this.db = new Database();
        db.init();
        this.graph = db.loadGraph();
        this.fw = new FloydWarshall(graph);
        fw.compute();
    }

    public PathResult shortestPath(String startName, String endName) {
        Node s = graph.getNodeByName(startName);
        Node t = graph.getNodeByName(endName);
        if (s == null || t == null) return null;
        int si = graph.indexOf(s.id);
        int ti = graph.indexOf(t.id);
        double dist = fw.getDistanceMatrix()[si][ti];
        if (Double.isInfinite(dist)) return null;
        var pathIdx = fw.reconstructPath(si, ti);
        var names = pathIdx.stream().map(i -> graph.nodes.get(i).name).toList();
        return new PathResult(dist, names);
    }

    @Override
    public void close() {
        db.close();
    }
}
