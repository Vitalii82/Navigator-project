package com.navigator.math;

import com.navigator.model.Graph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class FloydWarshall {
    private static final Logger logger = LogManager.getLogger(FloydWarshall.class);

    private final Graph graph;
    private double[][] dist;
    private int[][] next;

    public FloydWarshall(Graph graph) {
        this.graph = graph;
        logger.info("FloydWarshall initialized for graph with {} nodes and {} edges", graph.nodes.size(), graph.edges.size());
    }

    public void compute() {
        int n = graph.nodes.size();
        dist = new double[n][n];
        next = new int[n][n];

        logger.info("Initializing distance and next matrices");

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = (i == j) ? 0.0 : Double.POSITIVE_INFINITY;
                next[i][j] = -1;
            }
        }

        logger.info("Adding edges to distance matrix");
        for (var e : graph.edges) {
            int u = graph.indexOf(e.fromId);
            int v = graph.indexOf(e.toId);
            if (u == -1 || v == -1) {
                logger.warn("Edge {} -> {} skipped: node not found", e.fromId, e.toId);
                continue;
            }
            if (e.weight < dist[u][v]) {
                dist[u][v] = e.weight;
                next[u][v] = v;
                logger.debug("Edge {} -> {} with weight {} added", e.fromId, e.toId, e.weight);
            }
        }

        logger.info("Starting main Floyd-Warshall computation");
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (Double.isInfinite(dist[i][k])) continue;
                for (int j = 0; j < n; j++) {
                    double alt = dist[i][k] + dist[k][j];
                    if (alt < dist[i][j]) {
                        dist[i][j] = alt;
                        next[i][j] = next[i][k];
                        logger.debug("Updated dist[{}][{}] = {} via node {}", i, j, alt, k);
                    }
                }
            }
        }
        logger.info("Floyd-Warshall computation finished");
    }

    public double[][] getDistanceMatrix() {
        return dist;
    }

    public List<Integer> reconstructPath(int startIndex, int endIndex) {
        if (next[startIndex][endIndex] == -1) {
            logger.warn("No path exists from {} to {}", startIndex, endIndex);
            return List.of();
        }
        List<Integer> path = new ArrayList<>();
        int at = startIndex;
        path.add(at);
        while (at != endIndex) {
            at = next[at][endIndex];
            if (at == -1) {
                logger.warn("Path reconstruction failed from {} to {}", startIndex, endIndex);
                return List.of();
            }
            path.add(at);
        }
        logger.info("Path reconstructed from {} to {}: {}", startIndex, endIndex, path);
        return path;
    }
}
