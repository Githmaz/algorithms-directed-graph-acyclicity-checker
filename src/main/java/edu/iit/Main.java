/*
 * Name:       Sandaru Senevirathne
 * Student ID: 20232351
 * Module:     5SENG003W Algorithms
 * Class:      Main
 * Purpose:    Loads a graph from file and runs the acyclicity check.
 */

package edu.iit;

import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the directed graph acyclicity checker.
 * Accepts a filename from the command line or interactive prompt.
 * Looks up files in benchmarks/acyclic/ then benchmarks/cyclic/ on the
 * classpath, and falls back to a direct filesystem path if not found.
 */

public class Main {
    private static final String ACYCLIC_FOLDER    = "benchmarks/acyclic/";
    private static final String CYCLIC_FOLDER     = "benchmarks/cyclic/";
    private static final String DEFAULT_EXTENSION = ".txt";
    /**
     * Loads a graph from the given file, runs the acyclicity check,
     * and prints the result together.
     *
     * @param args  not used
     */
    public static void main(String[] args) {
        String fileName = readFileName();

        System.out.println("File Name: " + fileName);
        System.out.println();

        long startTime = System.currentTimeMillis();
        Graph graph    = loadGraph(fileName);

        if (graph == null) {
            System.err.println("Error: could not load '" + fileName + "'.");
            System.exit(1);
        }

        System.out.printf("Vertices: %d  |  Edges: %d%n%n",
                graph.vertexCount(), graph.edgeCount());

        boolean isAcyclic = AcyclicityChecker.isAcyclic(graph);

        if (isAcyclic) {
            System.out.println("RESULT: YES - the graph is acyclic.");
        } else {
            System.out.println("RESULT: NO  - the graph contains a cycle.");
            System.out.println();
            System.out.println("=== Finding cycle ===");
            List<Integer> cycle = AcyclicityChecker.findCycle(graph);
            if (cycle != null) System.out.println("Cycle: " + cycle);
        }

    }

    /**
     * Prompts the user to enter a filename interactively.
     * Appends DEFAULT_EXTENSION if no file extension is present.
     *
     * @return  the filename entered by the user
     */
    private static String readFileName() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter the input file name: ");
        String fileName = scanner.nextLine().trim();

        if (!fileName.contains(".")) fileName = fileName + DEFAULT_EXTENSION;
        return fileName;
    }

    /**
     * Resolves and loads a graph file, trying classpath benchmark folders
     * first, then falling back to a direct filesystem path.
     *
     * @param filename  the filename to resolve (with or without extension)
     * @return  the populated Graph, or null if not found anywhere
     */
    public static Graph loadGraph(String filename) {
        String name = filename.contains(".") ? filename : filename + DEFAULT_EXTENSION;

        Graph graph;

        graph = parseFromClasspath(ACYCLIC_FOLDER + name);
        if (graph != null) {
            System.out.println("(source: resources/" + ACYCLIC_FOLDER + ")");
            return graph;
        }

        graph = parseFromClasspath(CYCLIC_FOLDER + name);
        if (graph != null) {
            System.out.println("(source: resources/" + CYCLIC_FOLDER + ")");
            return graph;
        }

        graph = parseFromFile(name);
        if (graph != null) {
            System.out.println("(source: filesystem path)");
            return graph;
        }

        return null;
    }

    /**
     * Attempts to load a graph from the classpath.
     * Works both in IntelliJ and from a compiled JAR.
     *
     * @param resourcePath  classpath-relative path to the file
     * @return  the populated Graph, or null if not on the classpath
     */
    public static Graph parseFromClasspath(String resourcePath) {
        InputStream stream = Main.class.getClassLoader()
                                       .getResourceAsStream(resourcePath);
        if (stream == null) return null;
        return parseStream(stream, resourcePath);
    }

    /**
     * Attempts to load a graph from a direct filesystem path.
     *
     * @param filePath  full or relative path to the edge-list file
     * @return  the populated Graph, or null if the file is not found
     */
    public static Graph parseFromFile(String filePath) {
        try {
            return parseStream(new FileInputStream(filePath), filePath);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Core parser shared by all loading strategies.
     * Lines with a single integer are skipped (vertex-count header).
     * Lines with two integers are read as directed edges: from to.
     * Blank and malformed lines are skipped silently.
     *
     * @param inputStream  the stream to read from
     * @param sourceName   descriptive name used in diagnostic messages
     * @return  the populated Graph, or null on an IO error
     */
    public static Graph parseStream(InputStream inputStream, String sourceName) {
        Graph graph      = new Graph();
        int   lineNumber = 0;
        int   edgeCount  = 0;

        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] tokens = line.split("\\s+");

                //  skip single-integer header lines (vertex count declarations)
                if (tokens.length == 1) {
                    try { Integer.parseInt(tokens[0]); continue; }
                    catch (NumberFormatException ignored) {}
                }

                if (tokens.length < 2) continue;

                try {
                    graph.addEdge(Integer.parseInt(tokens[0]),
                                  Integer.parseInt(tokens[1]));
                    edgeCount++;
                } catch (NumberFormatException ignored) {}
            }

        } catch (IOException e) {
            System.err.println("IO error reading '" + sourceName + "': " + e.getMessage());
            return null;
        }

        System.out.printf("Parser: %d edge(s) read from %d line(s).%n", edgeCount, lineNumber);
        return graph;
    }

    /**
     * Silently loads a graph from the classpath without printing any output.
     * Used by PerformanceTest for file discovery and timing.
     *
     * @param resourcePath  classpath-relative path to the file
     * @return  the populated Graph, or null if not found
     */
    public static Graph parseFromClasspathSilent(String resourcePath) {
        InputStream stream = Main.class.getClassLoader()
                                       .getResourceAsStream(resourcePath);
        if (stream == null) return null;
        return parseStreamSilent(stream);
    }

    /**
     * Parses an edge-list stream without printing any output.
     * Identical logic to parseStream but fully silent.
     *
     * @param inputStream  the stream to read from
     * @return  the populated Graph, or null on error
     */
    private static Graph parseStreamSilent(InputStream inputStream) {
        Graph graph = new Graph();

        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] tokens = line.split("\\s+");

                if (tokens.length == 1) {
                    try { Integer.parseInt(tokens[0]); continue; }
                    catch (NumberFormatException ignored) {}
                }

                if (tokens.length < 2) continue;

                try {
                    graph.addEdge(Integer.parseInt(tokens[0]),
                                  Integer.parseInt(tokens[1]));
                } catch (NumberFormatException ignored) {}
            }

        } catch (IOException e) { return null; }

        return graph;
    }
}
