package com.navigator.util;

import java.util.List;

public class TableUtil {
    public static String pad(String s, int len) {
        if (s == null) s = "";
        if (s.length() >= len) return s;
        return s + " ".repeat(len - s.length());
    }
    public static String border(int len) {
        return "-".repeat(len);
    }
    public static void printNodes(List<com.navigator.model.Node> nodes) {
        String h1 = pad("ID", 4) + "  " + pad("NAME", 8) + "  " + pad("X", 8) + "  " + pad("Y", 8);
        System.out.println(h1);
        System.out.println(border(h1.length()));
        for (var n : nodes) {
            String line = pad(String.valueOf(n.id), 4) + "  " +
                    pad(n.name, 8) + "  " +
                    pad(String.format("%.3f", n.x), 8) + "  " +
                    pad(String.format("%.3f", n.y), 8);
            System.out.println(line);
        }
    }
    public static void printEdges(List<com.navigator.model.Edge> edges) {
        String h1 = pad("FROM", 6) + "  " + pad("TO", 6) + "  " + pad("WEIGHT", 10);
        System.out.println(h1);
        System.out.println(border(h1.length()));
        for (var e : edges) {
            String line = pad(String.valueOf(e.fromId), 6) + "  " +
                    pad(String.valueOf(e.toId), 6) + "  " +
                    pad(String.format("%.4f", e.weight), 10);
            System.out.println(line);
        }
    }
}
