/*
 * Name:       Sandaru Senevirathne
 * Student ID: 20232351
 * Module:     5SENG003W Algorithms
 * Class:      Graph
 * Purpose:    Represents a directed graph using a dual adjacency-list.
 */

package edu.iit;

import java.util.*;

/**
 * Represents a directed graph using a dual adjacency-list design.
 * Maintains outEdges, inEdges, and a dedicated sinks set
 * so that findSink() always runs in O(1).
 */
public class Graph {

    private final Map<Integer, Set<Integer>> outEdges;  //  vertex -> vertices it points to
    private final Map<Integer, Set<Integer>> inEdges;   //  vertex -> vertices pointing to it
    private final Set<Integer> sinks;                 //  vertices whose out-degree is zero

    /**
     * Creates an empty directed graph with no vertices or edges.
     */
    public Graph() {
        outEdges = new HashMap<>();
        inEdges  = new HashMap<>();
        sinks  = new HashSet<>();
    }

    /**
     * Deep-copy constructor. Duplicates all internal structures so algorithms
     * can work on a disposable copy without modifying the original graph.
     *
     * @param other  the graph to copy
     */
    public Graph(Graph other) {
        outEdges = new HashMap<>();
        inEdges  = new HashMap<>();
        sinks  = new HashSet<>(other.sinks);

        for (Map.Entry<Integer, Set<Integer>> entry : other.outEdges.entrySet()) {
            outEdges.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        for (Map.Entry<Integer, Set<Integer>> entry : other.inEdges.entrySet()) {
            inEdges.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
    }

    /**
     * Registers a vertex in the graph if it does not already exist.
     * A newly added vertex has out-degree zero and is placed into sinks.
     *
     * @param vertex  the integer label of the vertex to add
     */
    public void addVertex(int vertex) {
        if (!outEdges.containsKey(vertex)) {
            outEdges.put(vertex, new HashSet<>());
            inEdges.put(vertex, new HashSet<>());
            sinks.add(vertex);
        }
    }

    /**
     * Adds a directed edge from source to destination.
     * Either endpoint is created automatically if it does not already exist.
     * Duplicate edges are silently ignored.
     *
     * @param source       the label of the source vertex
     * @param destination  the label of the destination vertex
     */
    public void addEdge(int source, int destination) {
        addVertex(source);
        addVertex(destination);

        boolean isNewEdge = outEdges.get(source).add(destination);
        if (isNewEdge) {
            inEdges.get(destination).add(source);
            sinks.remove(source);    //  source now has an outgoing edge
        }
    }

    /**
     * Removes a sink vertex and all its incoming edges.
     * For each predecessor, removes the edge and checks if it has become a new sink.
     *
     * @param sink  the vertex to remove; must currently have out-degree zero
     * @throws IllegalArgumentException  if sink has outgoing edges
     */
    public void removeSink(int sink) {
        if (!sinks.contains(sink)) {
            throw new IllegalArgumentException(
                "Vertex " + sink + " is not a sink - it has outgoing edges.");
        }

        for (int predecessor : inEdges.get(sink)) {
            Set<Integer> predecessorOut = outEdges.get(predecessor);
            predecessorOut.remove(sink);

            if (predecessorOut.isEmpty()) {
                sinks.add(predecessor);    //  predecessor has become a new sink
            }
        }

        outEdges.remove(sink);
        inEdges.remove(sink);
        sinks.remove(sink);
    }

    /**
     * Returns any sink vertex (out-degree zero), or -1 if none exists.
     * Time complexity: O(1).
     *
     * @return  a sink vertex label, or -1
     */
    public int findSink() {
        if (sinks.isEmpty()) return -1;
        return sinks.iterator().next();
    }

    /**
     * Returns true if the graph contains no vertices.
     *
     * @return  true when the graph is empty
     */
    public boolean isEmpty() {
        return outEdges.isEmpty();
    }

    /**
     * Returns an unmodifiable view of all vertex labels in the graph.
     *
     * @return  unmodifiable set of vertex labels
     */
    public Set<Integer> getVertices() {
        return Collections.unmodifiableSet(outEdges.keySet());
    }

    /**
     * Returns an unmodifiable view of the out-neighbours of a vertex,
     * or an empty set if the vertex is not present.
     *
     * @param vertex  the vertex label to query
     * @return  unmodifiable set of successor labels
     */
    public Set<Integer> getOutEdges(int vertex) {
        Set<Integer> neighbours = outEdges.get(vertex);
        if (neighbours == null) return Collections.emptySet();
        return Collections.unmodifiableSet(neighbours);
    }

    /**
     * Returns the number of vertices currently in the graph.
     *
     * @return  vertex count
     */
    public int vertexCount() {
        return outEdges.size();
    }

    /**
     * Returns the total number of directed edges in the graph.
     *
     * @return  edge count
     */
    public int edgeCount() {
        int total = 0;
        for (Set<Integer> neighbours : outEdges.values()) {
            total += neighbours.size();
        }
        return total;
    }
}
