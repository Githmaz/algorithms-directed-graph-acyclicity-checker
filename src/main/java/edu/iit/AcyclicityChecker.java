/*
 * Name:       Sandaru Senevirathne
 * Student ID: 20232351
 * Module:     5SENG003W Algorithms
 * Class:      AcyclicityChecker
 * Purpose:    Checks if a graph is acyclic and finds a cycle if one exists.
 */

package edu.iit;

import java.util.*;

/**
 * Provides static methods to check whether a directed graph is acyclic
 * and to extract an explicit cycle when one exists.
 */
public class AcyclicityChecker {

    private AcyclicityChecker() {}

    /**
     * Determines whether the graph is acyclic using sink elimination,
     * printing each step so the result can be independently verified.
     * Works on a deep copy so the original graph is never modified.
     * Time complexity: O(V + E).
     *
     * @param original  the graph to test
     * @return  true if acyclic, false if a cycle exists
     */
    public static boolean isAcyclic(Graph original) {
        return isAcyclic(original, true);
    }

    /**
     * Same as isAcyclic(Graph) with optional console output.
     *
     * @param original  the graph to test
     * @param verbose   when false, all printed output is suppressed
     * @return  true if acyclic, false if a cycle exists
     */
    public static boolean isAcyclic(Graph original, boolean verbose) {
        Graph workingCopy = new Graph(original);

        if (verbose) {
            System.out.println("--- Sink Elimination Algorithm ---");
            System.out.printf("Initial state: %d vertices, %d edges%n",
                    workingCopy.vertexCount(), workingCopy.edgeCount());
        }

        int step = 1;

        while (!workingCopy.isEmpty()) {
            int sink = workingCopy.findSink();

            if (sink == -1) {
                if (verbose) {
                    System.out.printf("Step %-3d: No sink found - a cycle is present.%n", step);
                }
                return false;
            }

            if (verbose) {
                System.out.printf("Step %-3d: Removing sink %3d%n", step, sink);
            }

            workingCopy.removeSink(sink);
            step++;
        }

        if (verbose) {
            System.out.printf("All %d vertices removed - graph is ACYCLIC.%n", step - 1);
        }
        return true;
    }

    /**
     * Returns a list of vertex labels forming a cycle, or null if the graph is acyclic.
     * Traverses the graph using depth-first search, keeping track of visited vertices
     * and the current recursion stack to detect back edges that indicate cycles.
     * Time complexity: O(V + E).
     *
     * @param original  the graph to search
     * @return  closed cycle list, or null when the graph is acyclic
     */
    public static List<Integer> findCycle(Graph original) {
        Graph workingCopy = new Graph(original);

        //  strip all sinks - they cannot participate in any cycle
        int sink;
        while ((sink = workingCopy.findSink()) != -1) {
            workingCopy.removeSink(sink);
        }

        if (workingCopy.isEmpty()) return null;

        Set<Integer>          visited        = new HashSet<>();
        Set<Integer>          recursionStack = new HashSet<>();
        Map<Integer, Integer> parentMap      = new HashMap<>();

        for (int startVertex : workingCopy.getVertices()) {
            if (!visited.contains(startVertex)) {
                List<Integer> cycle = dfs(workingCopy, startVertex,
                                          visited, recursionStack, parentMap);
                if (cycle != null) return cycle;
            }
        }

        return null;
    }

    /**
     * Performs a depth-first search to detect cycles in the graph.
     * Uses a recursion stack to identify back edges which indicate cycles.
     *
     * @param graph          the working subgraph to search
     * @param current        the current vertex being explored
     * @param visited        set of all vertices visited so far
     * @param recursionStack set of vertices on the current DFS path
     * @param parentMap      mapping of each vertex to its parent in the DFS tree
     * @return  the cycle as a list if one is found, null otherwise
     */
    private static List<Integer> dfs(Graph graph, int current,
            Set<Integer> visited, Set<Integer> recursionStack,
            Map<Integer, Integer> parentMap) {

        visited.add(current);
        recursionStack.add(current);

        for (int neighbour : graph.getOutEdges(current)) {
            if (!visited.contains(neighbour)) {
                parentMap.put(neighbour, current);
                List<Integer> cycle = dfs(graph, neighbour,
                                          visited, recursionStack, parentMap);
                if (cycle != null) return cycle;
            } else if (recursionStack.contains(neighbour)) {
                return buildCyclePath(parentMap, current, neighbour);
            }
        }

        recursionStack.remove(current);
        return null;
    }

    /**
     * Extracts the cycle path from the DFS traversal by tracing back from
     * cycleEnd to cycleStart using the parent map, then reverses the result.
     *
     * @param parentMap   mapping of each vertex to its parent in the DFS tree
     * @param cycleEnd    the vertex where the back edge originates
     * @param cycleStart  the vertex where the back edge points
     * @return  closed cycle list [cycleStart, ..., cycleEnd, cycleStart]
     */
    private static List<Integer> buildCyclePath(Map<Integer, Integer> parentMap,
            int cycleEnd, int cycleStart) {

        List<Integer> cycle = new ArrayList<>();
        cycle.add(cycleStart);
        int current = cycleEnd;

        while (current != cycleStart) {
            cycle.add(current);
            current = parentMap.get(current);
        }

        cycle.add(cycleStart);
        Collections.reverse(cycle);
        return cycle;
    }
}
