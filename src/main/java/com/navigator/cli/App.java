package com.navigator.cli;

import com.navigator.db.exceptions.ServiceException;
import com.navigator.service.factory.ServiceFactory;
import com.navigator.service.intarfaces.IGraphService;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Scanner;

public class App {

    private static void printHelp() {
        System.out.println("""
Commands:
    help                 - show this help
    route START END      - compute shortest path from START to END
    exit | quit | q      - quit

Examples:
    route A C
""");
    }

    private static String norm(String s) {
        return s.trim().toUpperCase(Locale.ROOT);
    }

    private static void handleRoute(IGraphService service, String sRaw, String eRaw) throws ServiceException {
        String start = norm(sRaw);
        String end = norm(eRaw);
        if (start.isEmpty() || end.isEmpty()) {
            System.out.println("Usage: route START END");
            return;
        }
        Instant t0 = Instant.now();
        var result = service.shortestPath(start, end);
        long tookMs = Duration.between(t0, Instant.now()).toMillis();
        if (result == null) {
            System.out.println("No path found or unknown nodes.");
            return;
        }
        System.out.printf("Distance: %.4f (in %d ms)%n", result.distance, tookMs);
        System.out.println("Path: " + String.join(" -> ", result.path));
    }

    public static void main(String[] args) throws ServiceException {
        IGraphService service = ServiceFactory.createNavigatorService();
        Scanner sc = new Scanner(System.in);


        boolean ranFromArgs = false;
        if (args != null && args.length >= 1) {
            String cmd = args[0].toLowerCase(Locale.ROOT);
            if ("route".equals(cmd) && args.length >= 3) {
                handleRoute(service, args[1], args[2]);
                ranFromArgs = true;
            } else if ("help".equals(cmd)) {
                printHelp();
                ranFromArgs = true;
            }
            if (ranFromArgs) return;
        }

        System.out.println("Navigator project (CLI + Math). Type 'help'.");
        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) { System.out.println(); break; }
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println("Tip: type 'help'.");
                continue;
            }
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase(Locale.ROOT);

            switch (cmd) {
                case "help" -> printHelp();
                case "exit", "quit", "q" -> { System.out.println("Bye."); return; }
                case "route" -> {
                    if (parts.length < 3) { System.out.println("Usage: route START END"); break; }
                    handleRoute(service, parts[1], parts[2]);
                }
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }
}
