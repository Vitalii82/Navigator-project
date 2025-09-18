package com.navigator.db;

import com.navigator.model.Edge;
import com.navigator.model.Graph;
import com.navigator.model.Node;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database implements AutoCloseable {
    private Connection conn;

    public void init() {
        try {
            conn = DriverManager.getConnection("jdbc:h2:./navigator_db;AUTO_SERVER=TRUE");
            runSchemaIfNeeded();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean tableExists(String name) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getTables(null, null, name.toUpperCase(), null)) {
            return rs.next();
        }
    }

    private void runSchemaIfNeeded() {
        try {
            if (!tableExists("NODES") || !tableExists("EDGES")) {
                String sql = readResource("/db/schema.sql");
                try (Statement st = conn.createStatement()) {
                    st.execute(sql);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize schema", e);
        }
    }

    private String readResource(String path) throws Exception {
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) throw new IllegalStateException("Resource not found: " + path);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    public Graph loadGraph() {
        List<Node> nodes = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        try (Statement st = conn.createStatement()) {
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new Graph(nodes, edges);
    }

    @Override
    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignored) {}
    }
}
