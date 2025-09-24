package com.navigator.service;

import java.util.List;

public class PathResult {
    public final double distance;
    public final List<String> path;

    public PathResult(double distance, List<String> path) {
        this.distance = distance;
        this.path = path;
    }

    @Override
    public String toString() {
        return "PathResult{distance=" + distance + ", path=" + path + "}";
    }
}