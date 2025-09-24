package com.navigator.db.dao;

import com.navigator.db.conection.ConnectionPool;
import com.navigator.model.Edge;
import com.navigator.model.Node;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GraphDaoImpl implements GraphDao {

    private final ConnectionPool connectionPool = ConnectionPool.getInstance();

    private static final String SELECT_NODES = "SELECT id, name, x, y FROM nodes ORDER BY id";
    private static final String SELECT_EDGES = "SELECT from_id, to_id, weight FROM edges";
    private static final String SELECT_NODE_BY_ID = "SELECT id, name, x, y FROM nodes WHERE id = ?";
    private static final String SELECT_EDGE_BY_IDS = "SELECT from_id, to_id, weight FROM edges WHERE from_id = ? AND to_id = ?";
    private static final String SELECT_NODE_BY_NAME = "SELECT id, name, x, y FROM nodes WHERE name = ?";

    @Override
    public List<Node> getAllNodes() {
        List<Node> nodes = new ArrayList<>();
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(SELECT_NODES)) {
                while (rs.next()) {
                    nodes.add(new Node(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("x"),
                            rs.getDouble("y")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting nodes", e);
        } finally {
            if (conn != null) connectionPool.releaseConnection(conn);
        }
        return nodes;
    }

    @Override
    public List<Edge> getAllEdges() {
        List<Edge> edges = new ArrayList<>();
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(SELECT_EDGES)) {
                while (rs.next()) {
                    edges.add(new Edge(
                            rs.getInt("from_id"),
                            rs.getInt("to_id"),
                            rs.getDouble("weight")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting edges", e);
        } finally {
            if (conn != null) connectionPool.releaseConnection(conn);
        }
        return edges;
    }

    @Override
    public Optional<Node> getNodeById(int id) {
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_NODE_BY_ID)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new Node(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getDouble("x"),
                                rs.getDouble("y")
                        ));
                    }
                }
            }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException("Error getting node with id " + id, e);
        } finally {
            if (conn != null) connectionPool.releaseConnection(conn);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Edge> getEdgeById(int fromId, int toId) {
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_EDGE_BY_IDS)) {
                ps.setInt(1, fromId);
                ps.setInt(2, toId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new Edge(
                                rs.getInt("from_id"),
                                rs.getInt("to_id"),
                                rs.getDouble("weight")
                        ));
                    }
                }
            }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException("Error getting edge from " + fromId + " to " + toId, e);
        } finally {
            if (conn != null) connectionPool.releaseConnection(conn);
        }
        return Optional.empty();
    }


    @Override
    public Optional<Node> getNodeByName(String name) {
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(SELECT_NODE_BY_NAME)) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new Node(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getDouble("x"),
                                rs.getDouble("y")
                        ));
                    }
                }
            }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException("Error getting node with name " + name, e);
        } finally {
            if (conn != null) connectionPool.releaseConnection(conn);
        }
        return Optional.empty();
    }
}
