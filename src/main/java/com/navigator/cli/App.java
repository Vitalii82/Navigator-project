package com.navigator.cli;

import com.navigator.db.exceptions.ServiceException;
import com.navigator.model.Edge;
import com.navigator.model.Node;
import com.navigator.service.factory.ServiceFactory;
import com.navigator.service.intarfaces.IGraphService;
import com.navigator.util.TableUtil;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class App {

    private static void printHelp() {
        System.out.println("""
Commands:
  help                         - show commands
  route START END              - shortest path from START to END
  list-nodes                   - print all nodes
  list-edges                   - print all edges
  add-node NAME X Y            - (not available yet) create node
  add-edge FROM TO WEIGHT      - (not available yet) create edge
  remove-node NAME             - (not available yet) delete node
  remove-edge FROM TO          - (not available yet) delete edge
  exit | quit | q              - quit

Examples:
    route A C
    list-nodes
""");
    }

    private static String norm(String s) { return s.trim().toUpperCase(Locale.ROOT); }

    private static void handleRoute(IGraphService service, String sRaw, String eRaw) throws ServiceException {
        String start = norm(sRaw);
        String end = norm(eRaw);
        if (start.isEmpty() || end.isEmpty()) { System.out.println("Usage: route START END"); return; }
        Instant t0 = Instant.now();
        var result = service.shortestPath(start, end);
        long took = Duration.between(t0, Instant.now()).toMillis();
        System.out.printf("Distance: %.4f (in %d ms)%n", result.distance(), took);
        System.out.println("Path: " + String.join(" -> ", result.path()));
    }

    private static void listNodes(IGraphService service) throws ServiceException {
        List<Node> nodes = service.getAllNodes();
        if (nodes.isEmpty()) { System.out.println("No nodes."); return; }
        TableUtil.printNodes(nodes);
    }

    private static void listEdges(IGraphService service) throws ServiceException {
        List<Edge> edges = service.getAllEdges();
        if (edges.isEmpty()) { System.out.println("No edges."); return; }
        TableUtil.printEdges(edges);
    }

    private static void notAvailable() {
        System.out.println("Write operations are not available in the current service API.");
        System.out.println("Ask teammate to expose DAO methods via IGraphService (save/update/delete).");
    }

    public static void main(String[] args) throws ServiceException {
        boolean argsMode = (args != null && args.length > 0);
        if (argsMode) {
            String cmd = args[0].toLowerCase(Locale.ROOT);
            IGraphService service = ServiceFactory.createNavigatorService();
            switch (cmd) {
                case "route" -> {
                    if (args.length < 3) { System.out.println("Usage: route START END"); return; }
                    handleRoute(service, args[1], args[2]);
                }
                case "help" -> printHelp();
                case "list-nodes" -> listNodes(service);
                case "list-edges" -> listEdges(service);
                case "add-node", "add-edge", "remove-node", "remove-edge" -> notAvailable();
                default -> System.out.println("Unknown command. Type 'help'.");
            }
            return;
        }

        System.out.println("Navigator project (CLI + Math). Type 'help'.");
        try (Scanner sc = new Scanner(System.in)) {
            IGraphService service = ServiceFactory.createNavigatorService();
            while (true) {
                System.out.print("> ");
                if (!sc.hasNextLine()) { System.out.println(); break; }
                String line = sc.nextLine().trim();
                if (line.isEmpty()) { System.out.println("Tip: type 'help'."); continue; }
                String[] parts = line.split("\\s+");
                String cmd = parts[0].toLowerCase(Locale.ROOT);
                switch (cmd) {
                    case "help" -> printHelp();
                    case "exit", "quit", "q" -> { System.out.println("Bye."); return; }
                    case "route" -> {
                        if (parts.length < 3) { System.out.println("Usage: route START END"); break; }
                        handleRoute(service, parts[1], parts[2]);
                    }
                    case "list-nodes" -> listNodes(service);
                    case "list-edges" -> listEdges(service);
                    case "add-node", "add-edge", "remove-node", "remove-edge" -> notAvailable();
                    default -> System.out.println("Unknown command. Type 'help'.");
                }
            }
        }
    }
}
