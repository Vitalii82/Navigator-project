package com.navigator;

import com.navigator.model.Node;
import com.navigator.model.Edge;
import com.navigator.service.PathResult;
import com.navigator.service.intarfaces.IGraphService;
import com.navigator.service.factory.ServiceFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

public class App {

    private static final Logger logger = LogManager.getLogger(App.class);

    private static void printBanner() {
        try {
            File f = new File("NavigatorLogo.txt");
            if (f.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) logger.info(line);
                }
                return;
            }
        } catch (Exception ignored) {}
        logger.info("===== NAVIGATOR PROJECT v1.0 =====");
        logger.info("Type 'help' to see available commands. 'exit' to quit.");
        logger.info("==================================");
    }

    private static void printHelp() {
        logger.info("Commands:");
        logger.info("  help");
        logger.info("  info");
        logger.info("  list-nodes");
        logger.info("  list-edges");
        logger.info("  add-node <NAME> <X> <Y>");
        logger.info("  add-edge <A> <B> <W>");
        logger.info("  remove-node <NAME>");
        logger.info("  remove-edge <A> <B>");
        logger.info("  route <START> <END>");
        logger.info("  exit");
    }

    private static void printInfo() {
        logger.info("Navigator Project — console pathfinding with SQL storage and Floyd–Warshall.");
        logger.info("Authors: Vitalii Svinovei (CLI/Math), Vadym Skryp (DB/Service).");
        logger.info("Use 'help' to see commands.");
    }

    private static String pathToArrowString(List<String> path) {
        return String.join(" \u2192 ", path);
    }

    private static void printNodes(List<Node> nodes) {
        logger.info("+----+-------+---------+---------+");
        logger.info("| ID | NAME  |   X     |   Y     |");
        logger.info("+----+-------+---------+---------+");
        for (Node n : nodes) {
            logger.info(String.format(Locale.US, "| %2d | %-5s | %7.3f | %7.3f |", n.id, n.name, n.x, n.y));
        }
        logger.info("+----+-------+---------+---------+");
    }

    private static void printEdges(List<Edge> edges) {
        logger.info("+---------+---------+---------+");
        logger.info("| FROM_ID |  TO_ID  | WEIGHT  |");
        logger.info("+---------+---------+---------+");
        for (Edge e : edges) {
            logger.info(String.format(Locale.US, "| %7d | %7d | %7.3f |", e.fromId, e.toId, e.weight));
        }
        logger.info("+---------+---------+---------+");
    }

    public static void main(String[] args) throws Exception {
        printBanner();
        IGraphService service = ServiceFactory.create(); // MyBatis service by default
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            logger.info("> ");
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
                        logger.info("Bye!");
                        return;
                    case "list-nodes":
                        printNodes(service.getAllNodes());
                        break;
                    case "list-edges":
                        printEdges(service.getAllEdges());
                        break;
                    case "add-node":
                        if (parts.length != 4) {
                            logger.info("Usage: add-node <NAME> <X> <Y>");
                            break;
                        }
                        String name = parts[1];
                        double x = Double.parseDouble(parts[2]);
                        double y = Double.parseDouble(parts[3]);
                        long t0 = System.nanoTime();
                        service.addNode(name, x, y);
                        long dt = System.nanoTime() - t0;
                        logger.info(String.format("Node '%s' added (%.2f ms).", name, dt/1e6));
                        break;
                    case "add-edge":
                        if (parts.length != 4) {
                            logger.info("Usage: add-edge <A> <B> <W>");
                            break;
                        }
                        String a = parts[1], b = parts[2];
                        double w = Double.parseDouble(parts[3]);
                        t0 = System.nanoTime();
                        service.addEdge(a, b, w);
                        dt = System.nanoTime() - t0;
                        logger.info(String.format("Edge %s -> %s added (%.2f ms).", a, b, dt/1e6));
                        break;
                    case "remove-node":
                        if (parts.length != 2) {
                            logger.info("Usage: remove-node <NAME>");
                            break;
                        }
                        name = parts[1];
                        t0 = System.nanoTime();
                        service.removeNode(name);
                        dt = System.nanoTime() - t0;
                        logger.info(String.format("Node '%s' removed (%.2f ms).", name, dt/1e6));
                        break;
                    case "remove-edge":
                        if (parts.length != 3) {
                            logger.info("Usage: remove-edge <A> <B>");
                            break;
                        }
                        a = parts[1]; b = parts[2];
                        t0 = System.nanoTime();
                        service.removeEdge(a, b);
                        dt = System.nanoTime() - t0;
                        logger.info(String.format("Edge %s -> %s removed (%.2f ms).", a, b, dt/1e6));
                        break;
                    case "route":
                        if (parts.length != 3) {
                            logger.info("Usage: route <START> <END>");
                            break;
                        }
                        String start = parts[1], end = parts[2];
                        t0 = System.nanoTime();
                        PathResult res = service.shortestPath(start, end);
                        dt = System.nanoTime() - t0;
                        if (res == null || res.path == null || res.path.isEmpty()) {
                            logger.info("No path found.");
                        } else {
                            String p = pathToArrowString(res.path);
                            logger.info(String.format("Path: %s (total: %.3f). Time: %.2f ms", p, res.getDistance(), dt/1e6));
                        }
                        break;
                    default:
                        logger.info("Unknown command. Type 'help'.");
                }
            } catch (Exception ex) {
                logger.error("Error: {}", ex.getMessage(), ex);
            }
        }
    }
}
