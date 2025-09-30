package com.navigator;

import com.navigator.model.Node;
import com.navigator.model.Edge;
import com.navigator.service.PathResult;
import com.navigator.service.intarfaces.IGraphService;
import com.navigator.service.factory.ServiceFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

public class App {

    private static void printBanner() {
        try {
            File f = new File("NavigatorLogo.txt");
            if (f.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) System.out.println(line);
                }
                return;
            }
        } catch (Exception ignored) {}
        System.out.println("===== NAVIGATOR PROJECT v1.0 =====");
        System.out.println("Type 'help' to see available commands. 'exit' to quit.");
        System.out.println("==================================");
    }

    private static void printHelp() {
        System.out.println("Commands:");
        System.out.println("  help");
        System.out.println("  info");
        System.out.println("  list-nodes");
        System.out.println("  list-edges");
        System.out.println("  add-node <NAME> <X> <Y>");
        System.out.println("  add-edge <A> <B> <W>");
        System.out.println("  remove-node <NAME>");
        System.out.println("  remove-edge <A> <B>");
        System.out.println("  route <START> <END>");
        System.out.println("  exit");
    }

    private static void printInfo() {
        System.out.println("Navigator Project — console pathfinding with SQL storage and Floyd–Warshall.");
        System.out.println("Authors: Vitalii Svinovei (CLI/Math), Vadym Skryp (DB/Service).");
        System.out.println("Use 'help' to see commands.");
    }

    private static String pathToArrowString(List<String> path) {
        return String.join(" \u2192 ", path);
    }

    private static void printNodes(List<Node> nodes) {
        System.out.println("+----+-------+---------+---------+");
        System.out.println("| ID | NAME  |   X     |   Y     |");
        System.out.println("+----+-------+---------+---------+");
        for (Node n : nodes) {
            System.out.printf(Locale.US, "| %2d | %-5s | %7.3f | %7.3f |%n", n.id, n.name, n.x, n.y);
        }
        System.out.println("+----+-------+---------+---------+");
    }

    private static void printEdges(List<Edge> edges) {
        System.out.println("+---------+---------+---------+");
        System.out.println("| FROM_ID |  TO_ID  | WEIGHT  |");
        System.out.println("+---------+---------+---------+");
        for (Edge e : edges) {
            System.out.printf(Locale.US, "| %7d | %7d | %7.3f |%n", e.fromId, e.toId, e.weight);
        }
        System.out.println("+---------+---------+---------+");
    }

    public static void main(String[] args) throws Exception {
        printBanner();
        IGraphService service = ServiceFactory.create(); // MyBatis service by default
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            String line = in.readLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase(Locale.ROOT);

            try {
                switch (cmd) {
                    case "help":
                        printHelp();
                        break;
                    case "info":
                        printInfo();
                        break;
                    case "exit":
                        System.out.println("Bye!");
                        return;
                    case "list-nodes":
                        printNodes(service.getAllNodes());
                        break;
                    case "list-edges":
                        printEdges(service.getAllEdges());
                        break;
                    case "add-node":
                        if (parts.length != 4) {
                            System.out.println("Usage: add-node <NAME> <X> <Y>");
                            break;
                        }
                        String name = parts[1];
                        double x = Double.parseDouble(parts[2]);
                        double y = Double.parseDouble(parts[3]);
                        long t0 = System.nanoTime();
                        service.addNode(name, x, y);
                        long dt = System.nanoTime() - t0;
                        System.out.printf("Node '%s' added (%.2f ms).%n", name, dt/1e6);
                        break;
                    case "add-edge":
                        if (parts.length != 4) {
                            System.out.println("Usage: add-edge <A> <B> <W>");
                            break;
                        }
                        String a = parts[1], b = parts[2];
                        double w = Double.parseDouble(parts[3]);
                        t0 = System.nanoTime();
                        service.addEdge(a, b, w);
                        dt = System.nanoTime() - t0;
                        System.out.printf("Edge %s -> %s added (%.2f ms).%n", a, b, dt/1e6);
                        break;
                    case "remove-node":
                        if (parts.length != 2) {
                            System.out.println("Usage: remove-node <NAME>");
                            break;
                        }
                        name = parts[1];
                        t0 = System.nanoTime();
                        service.removeNode(name);
                        dt = System.nanoTime() - t0;
                        System.out.printf("Node '%s' removed (%.2f ms).%n", name, dt/1e6);
                        break;
                    case "remove-edge":
                        if (parts.length != 3) {
                            System.out.println("Usage: remove-edge <A> <B>");
                            break;
                        }
                        a = parts[1]; b = parts[2];
                        t0 = System.nanoTime();
                        service.removeEdge(a, b);
                        dt = System.nanoTime() - t0;
                        System.out.printf("Edge %s -> %s removed (%.2f ms).%n", a, b, dt/1e6);
                        break;
                    case "route":
                        if (parts.length != 3) {
                            System.out.println("Usage: route <START> <END>");
                            break;
                        }
                        String start = parts[1], end = parts[2];
                        t0 = System.nanoTime();
                        PathResult res = service.shortestPath(start, end);
                        dt = System.nanoTime() - t0;
                        if (res == null || res.path == null || res.path.isEmpty()) {
                            System.out.println("No path found.");
                        } else {
                            String p = pathToArrowString(res.path);
                            System.out.printf("Path: %s (total: %.3f). Time: %.2f ms%n", p, res.getDistance(), dt/1e6);
                        }
                        break;
                    default:
                        System.out.println("Unknown command. Type 'help'.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}
