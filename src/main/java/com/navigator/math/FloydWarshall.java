package com.navigator.math;

import com.navigator.model.Graph;
import java.util.ArrayList;
import java.util.List;

public class FloydWarshall {
    private final Graph graph;
    private double[][] dist;
    private int[][] next;

    public FloydWarshall(Graph graph) {
        this.graph = graph;
    }

    public void compute() {
        int n = graph.nodes.size();
        dist = new double[n][n];
        next = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                dist[i][j] = (i == j) ? 0.0 : Double.POSITIVE_INFINITY;
                next[i][j] = -1;
            }
        }
        for (var e : graph.edges) {
            int u = graph.indexOf(e.fromId);
            int v = graph.indexOf(e.toId);
            if (u == -1 || v == -1) continue;
            if (e.weight < dist[u][v]) {
                dist[u][v] = e.weight;
                next[u][v] = v;
            }
        }

        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (Double.isInfinite(dist[i][k])) continue;
                for (int j = 0; j < n; j++) {
                    double alt = dist[i][k] + dist[k][j];
                    if (alt < dist[i][j]) {
                        dist[i][j] = alt;
                        next[i][j] = next[i][k];
                    }
                }
            }
        }
    }

    public double[][] getDistanceMatrix() {
        return dist;
    }

    public List<Integer> reconstructPath(int startIndex, int endIndex) {
        if (next[startIndex][endIndex] == -1) return List.of();
        List<Integer> path = new ArrayList<>();
        int at = startIndex;
        path.add(at);
        while (at != endIndex) {
            at = next[at][endIndex];
            if (at == -1) return List.of();
            path.add(at);
        }
        return path;
    }
}
