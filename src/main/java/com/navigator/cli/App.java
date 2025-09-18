package com.navigator.cli;

import com.navigator.service.NavigatorService;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        System.out.println("Navigator project (Floyd–Warshall). Type 'exit' to quit.");
        try (NavigatorService service = new NavigatorService();
                Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.print("Enter START and END (e.g., A C): ");
                String line = sc.nextLine().trim();
                if (line.equalsIgnoreCase("exit")) break;
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    System.out.println("Please provide two node names, e.g. 'A C'.");
                    continue;
                }
                var result = service.shortestPath(parts[0], parts[1]);
                if (result == null) {
                    System.out.println("No path found or unknown nodes.");
                } else {
                    System.out.printf("Distance: %.4f%n", result.distance);
                    System.out.println("Path: " + String.join(" -> ", result.path));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Bye.");
    }
}
