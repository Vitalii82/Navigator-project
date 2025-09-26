package com.navigator.service;

import java.util.List;

public class PathResult {
    public final double distance;
    public final List<String> path;

    public PathResult(double distance, List<String> path) {
        this.distance = distance;
        this.path = path;
    }

    public double distance() {
        return distance;
    }

    public List<String> path() {
        return path;
    }

    @Override
    public String toString() {
        return "PathResult{distance=" + distance + ", path=" + path + "}";
    }
}