package com.navigator.service;

import java.util.List;

public class PathResult {
    public List<String> path;

    public PathResult(double distance, List<String> path) {
        this.distance = distance;
        this.path = path;
    }

    // Support either field name used by service implementation
    public Double totalWeight;
    public Double distance;

    public double getDistance() {
        if (totalWeight != null) return totalWeight;
        if (distance != null) return distance;
        return Double.NaN;
    }
}
