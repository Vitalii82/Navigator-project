package com.navigator.service;

import com.navigator.db.conection.ConnectionPool;
import com.navigator.math.FloydWarshall;
import com.navigator.model.Edge;
import com.navigator.model.Graph;
import com.navigator.model.Node;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NavigatorService implements AutoCloseable {

    private final ConnectionPool connectionPool = ConnectionPool.getInstance();
    private final Graph graph;
    private final FloydWarshall fw;

    public static class PathResult {
        public final double distance;
        public final List<String> path;
        public PathResult(double d, List<String> p) { this.distance = d; this.path = p; }
    }

    public NavigatorService() {
        this.graph = loadGraphFromDB();
        this.fw = new FloydWarshall(graph);
        fw.compute();
    }

    private Graph loadGraphFromDB() {
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();

        try (Connection conn = connectionPool.getConnection();
             Statement st = conn.createStatement()) {

            try (ResultSet rs = st.executeQuery("SELECT id, name, x, y FROM nodes ORDER BY id")) {
                while (rs.next()) {
                    nodes.add(new Node(rs.getInt("id"), rs.getString("name"),
                            rs.getDouble("x"), rs.getDouble("y")));
                }
            }

            try (ResultSet rs = st.executeQuery("SELECT from_id, to_id, weight FROM edges")) {
                while (rs.next()) {
                    edges.add(new Edge(rs.getInt("from_id"), rs.getInt("to_id"),
                            rs.getDouble("weight")));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load graph from DB", e);
        }

        return new Graph(nodes, edges);
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

    }
}
