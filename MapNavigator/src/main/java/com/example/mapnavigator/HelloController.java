package com.example.mapnavigator;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class HelloController {
    @FXML
    private ComboBox<String> sourceComboBox;

    @FXML
    private ComboBox<String> destinationComboBox;

    @FXML
    private Label resultLabel;

    @FXML
    private WebView mapView;

    @FXML
    private TextField newSourceField;

    @FXML
    private TextField newDestinationField;

    @FXML
    private TextField newWeightField;

    @FXML
    private Canvas graphCanvas;

    private final Graph graph = new Graph();

    private static final String GRAPH_DATA_FILE = "graphData.ser";

    public HelloController() {
        try {
            graph.loadGraph(GRAPH_DATA_FILE);
        } catch (IOException | ClassNotFoundException e) {
            initializeGraph();
        }
    }

    @FXML
    public void initialize() {
        List<String> cities = graph.getAdjacencyList().keySet().stream().sorted().collect(Collectors.toList());
        sourceComboBox.setItems(FXCollections.observableArrayList(cities));
        destinationComboBox.setItems(FXCollections.observableArrayList(cities));
        drawGraph();
    }

    @FXML
    protected void onFindPathClick() {
        String source = sourceComboBox.getValue();
        String destination = destinationComboBox.getValue();

        if (source == null || destination == null) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please select both source and destination.");
            return;
        }

        List<String> path = graph.shortestPath(source, destination);

        if (path.isEmpty()) {
            resultLabel.setText("No path found between " + source + " and " + destination);
        } else {
            String result = "Shortest path: " + String.join(" -> ", path);
            int totalDistance = calculateTotalDistance(path);
            resultLabel.setText(result + " (Total distance: " + totalDistance + " km)");

            // Load the map showing the path
            loadMap(path);
        }
    }

    private int calculateTotalDistance(List<String> path) {
        int totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            String source = path.get(i);
            String destination = path.get(i + 1);
            for (Graph.Edge edge : graph.getAdjacencyList().get(source)) {
                if (edge.destination.equals(destination)) {
                    totalDistance += edge.weight;
                    break;
                }
            }
        }
        return totalDistance;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadMap(List<String> path) {
        String baseUrl = "https://www.google.com/maps/dir/";
        String query = String.join("/", path);
        String mapUrl = baseUrl + query.replace(" ", "+");
        mapView.getEngine().load(mapUrl);
    }


    @FXML
    protected void onSaveGraphClick() {
        try {
            graph.saveGraph(GRAPH_DATA_FILE);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Graph data saved successfully.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Save Error", "Failed to save graph data.");
        }
    }

    @FXML
    protected void onLoadGraphClick() {
        try {
            graph.loadGraph(GRAPH_DATA_FILE);
            initialize();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Graph data loaded successfully.");
        } catch (IOException | ClassNotFoundException e) {
            showAlert(Alert.AlertType.ERROR, "Load Error", "Failed to load graph data.");
        }
    }

    @FXML
    protected void onAddEdgeClick() {
        String newSource = newSourceField.getText().trim();
        String newDestination = newDestinationField.getText().trim();
        String weightText = newWeightField.getText().trim();

        if (newSource.isEmpty() || newDestination.isEmpty() || weightText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please fill in all fields.");
            return;
        }

        try {
            int weight = Integer.parseInt(weightText);
            graph.addEdge(newSource, newDestination, weight);
            showAlert(Alert.AlertType.INFORMATION, "Success", "New edge added successfully.");
            initialize();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Weight must be a number.");
        }
    }

    private void drawGraph() {
        GraphicsContext gc = graphCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, graphCanvas.getWidth(), graphCanvas.getHeight());

        List<String> cities = graph.getAdjacencyList().keySet().stream().sorted().collect(Collectors.toList());
        double centerX = graphCanvas.getWidth() / 2;
        double centerY = graphCanvas.getHeight() / 2;
        double radius = Math.min(centerX, centerY) - 50;
        double angleIncrement = 2 * Math.PI / cities.size();
        double angle = 0;

        // Draw nodes (cities)
        for (String city : cities) {
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            gc.setFill(Color.BLUE);
            gc.fillOval(x - 10, y - 10, 20, 20);
            gc.setFill(Color.WHITE);
            gc.fillText(city, x - 10, y - 15);
            angle += angleIncrement;
        }

        // Draw edges
        gc.setStroke(Color.BLACK);
        for (String source : cities) {
            double startX = centerX + radius * Math.cos(angleIncrement * cities.indexOf(source));
            double startY = centerY + radius * Math.sin(angleIncrement * cities.indexOf(source));
            for (Graph.Edge edge : graph.getAdjacencyList().get(source)) {
                String destination = edge.destination;
                double endX = centerX + radius * Math.cos(angleIncrement * cities.indexOf(destination));
                double endY = centerY + radius * Math.sin(angleIncrement * cities.indexOf(destination));
                gc.strokeLine(startX, startY, endX, endY);
            }
        }
    }

    private void initializeGraph() {
        graph.addEdge("Mumbai", "Pune", 150);
        graph.addEdge("Mumbai", "Delhi", 1400);
        graph.addEdge("Delhi", "Pune", 1300);
        graph.addEdge("Pune", "Chennai", 1000);
        graph.addEdge("Chennai", "Kolkata", 1600);
        graph.addEdge("Delhi", "Kolkata", 1500);
        graph.addEdge("Mumbai", "Ahmedabad", 500);
        graph.addEdge("Ahmedabad", "Delhi", 950);
        graph.addEdge("Delhi", "Chennai", 2100);
        graph.addEdge("Chennai", "Bangalore", 350);
        graph.addEdge("Bangalore", "Hyderabad", 570);
        graph.addEdge("Hyderabad", "Mumbai", 710);
        graph.addEdge("Kolkata", "Patna", 620);
        graph.addEdge("Patna", "Delhi", 1100);
        graph.addEdge("Patna", "Ranchi", 330);
        graph.addEdge("Ranchi", "Kolkata", 400);
        graph.addEdge("Kolkata", "Bhubaneswar", 440);
        graph.addEdge("Bhubaneswar", "Hyderabad", 980);
        graph.addEdge("Hyderabad", "Chennai", 630);
        graph.addEdge("New York", "Los Angeles", 4500);
        graph.addEdge("New York", "Chicago", 1200);
        graph.addEdge("New York", "Houston", 2600);
        graph.addEdge("Los Angeles", "San Francisco", 600);
        graph.addEdge("Los Angeles", "Houston", 2400);
        graph.addEdge("Chicago", "Houston", 1600);
        graph.addEdge("Chicago", "Atlanta", 1000);
        graph.addEdge("Atlanta", "Miami", 800);
        graph.addEdge("Houston", "Miami", 1500);
        graph.addEdge("San Francisco", "Seattle", 1300);
        graph.addEdge("Seattle", "Denver", 2300);
        graph.addEdge("Denver", "Chicago", 1600);
        graph.addEdge("Denver", "Houston", 1400);
        graph.addEdge("Miami", "Washington DC", 1600);
        graph.addEdge("Washington DC", "New York", 400);
        graph.addEdge("New York", "Boston", 300);
        graph.addEdge("Boston", "Chicago", 1600);
        graph.addEdge("Boston", "San Francisco", 5000);
        graph.addEdge("San Francisco", "Las Vegas", 900);
        graph.addEdge("Las Vegas", "Houston", 2400);
    }
}
