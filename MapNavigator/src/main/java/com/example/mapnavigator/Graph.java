package com.example.mapnavigator;

import java.io.*;
import java.util.*;

public class Graph implements Serializable {
    private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

    public static class Edge implements Serializable {
        String destination;
        int weight;

        Edge(String destination, int weight) {
            this.destination = destination;
            this.weight = weight;
        }
    }

    public void addEdge(String source, String destination, int weight) {
        adjacencyList.computeIfAbsent(source, k -> new ArrayList<>()).add(new Edge(destination, weight));
        adjacencyList.computeIfAbsent(destination, k -> new ArrayList<>()).add(new Edge(source, weight));
    }

    public List<String> shortestPath(String source, String destination) {
        // Dijkstra's algorithm implementation
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        PriorityQueue<String> nodes = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (String vertex : adjacencyList.keySet()) {
            if (vertex.equals(source)) {
                distances.put(vertex, 0);
            } else {
                distances.put(vertex, Integer.MAX_VALUE);
            }
            nodes.add(vertex);
        }

        while (!nodes.isEmpty()) {
            String closest = nodes.poll();
            if (closest.equals(destination)) {
                List<String> path = new ArrayList<>();
                while (previousNodes.containsKey(closest)) {
                    path.add(closest);
                    closest = previousNodes.get(closest);
                }
                path.add(source);
                Collections.reverse(path);
                return path;
            }

            if (distances.get(closest) == Integer.MAX_VALUE) {
                break;
            }

            for (Edge neighbor : adjacencyList.getOrDefault(closest, new ArrayList<>())) {
                int alt = distances.get(closest) + neighbor.weight;
                if (alt < distances.get(neighbor.destination)) {
                    distances.put(neighbor.destination, alt);
                    previousNodes.put(neighbor.destination, closest);
                    nodes.add(neighbor.destination);
                }
            }
        }
        return new ArrayList<>();
    }

    public Map<String, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

    public void saveGraph(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(adjacencyList);
        }
    }

    public void loadGraph(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            Map<String, List<Edge>> loadedList = (Map<String, List<Edge>>) in.readObject();
            adjacencyList.clear();
            adjacencyList.putAll(loadedList);
        }
    }
}
