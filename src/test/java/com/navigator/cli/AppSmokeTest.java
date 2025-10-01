package com.navigator.cli;

import com.navigator.App;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppSmokeTest {

    @Test
    void routeCommandPrintsPath() throws Exception {
        String input = "route A C\nexit\n";
        InputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        PrintStream originalOut = System.out;
        InputStream originalIn = System.in;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            System.setIn(in);
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            App.main(new String[]{});
        } finally {
            System.setOut(originalOut);
            System.setIn(originalIn);
        }
        String output = out.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Path:"), "should print Path:");
    }
}
