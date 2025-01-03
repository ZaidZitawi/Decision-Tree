package com.example.decisiontree;

import com.example.decisiontree.DataSet.Mushroom;
import com.example.decisiontree.DataSet.MushroomDataSet;
import com.example.decisiontree.Metrics.AccuracyMetrics;
import com.example.decisiontree.Tree.DecisionTree;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

public class MushroomDecisionTree extends Application {

    private TextArea decisionTreeArea;
    private TableView<Map.Entry<String, Double>> gainTable;
    private TableView<Map.Entry<String, Double>> entropyTable;
    private TextField txtAccuracy, txtPrecision, txtRecall, txtF1Score;
    private RadioButton rbInfoGain, rbGainRatio, rbTrainingOutput, rbTestingOutput;
    private MushroomDataSet dataSet;
    private List<Mushroom> trainingData, testData;
    private DecisionTree builtDecisionTree;
    private Button btnBuildTree;

    private static final List<String> ALL_ATTRIBUTES = Arrays.asList(
            "CAP-SHAPE","CAP-SURFACE","CAP-COLOR","BRUISES","ODOR","GILL-ATTACHMENT","GILL-SPACING",
            "GILL-SIZE","GILL-COLOR","STALK-SHAPE","STALK-ROOT","STALK-SURFACE-ABOVE-RING",
            "STALK-SRFACE-UNDER-RING","STALK-COLOR-ABOVE-RING","STALK-COLOR-BELOW-RING",
            "VEIL-TYPE","VEIL-COLOR","RING-NUMBER","RING-TYPE","SPORE-PRINT-COLOR","POPULATION","HABITAT"
    );

    @Override
    public void start(Stage primaryStage) {
        dataSet = new MushroomDataSet();
        boolean dataLoaded = loadData();

        decisionTreeArea = new TextArea();
        decisionTreeArea.setPromptText("Decision Tree Visualization...");
        decisionTreeArea.setWrapText(true);
        decisionTreeArea.setEditable(false);
        decisionTreeArea.getStyleClass().add("decision-tree-area");

        VBox leftVBox = new VBox(new Label("Decision Tree"), decisionTreeArea);
        leftVBox.setSpacing(5);
        leftVBox.setAlignment(Pos.TOP_LEFT);
        leftVBox.setPrefWidth(600);
        VBox.setVgrow(decisionTreeArea, Priority.ALWAYS);
        leftVBox.getStyleClass().add("left-vbox");

        ToggleGroup dataToggleGroup = new ToggleGroup();
        rbTrainingOutput = new RadioButton("Training Data");
        rbTestingOutput = new RadioButton("Testing Data");
        rbTrainingOutput.setToggleGroup(dataToggleGroup);
        rbTestingOutput.setToggleGroup(dataToggleGroup);
        rbTrainingOutput.setSelected(true);

        HBox dataOutputToggleBox = new HBox(10, new Label("Select Data for Metrics:"), rbTrainingOutput, rbTestingOutput);
        dataOutputToggleBox.setAlignment(Pos.CENTER_LEFT);

        ToggleGroup metricToggleGroup = new ToggleGroup();
        rbInfoGain = new RadioButton("Information Gain");
        rbGainRatio = new RadioButton("Gain Ratio");
        rbInfoGain.setToggleGroup(metricToggleGroup);
        rbGainRatio.setToggleGroup(metricToggleGroup);
        rbInfoGain.setSelected(true);

        HBox metricToggleBox = new HBox(10, new Label("Select Algorithm:"), rbInfoGain, rbGainRatio);
        metricToggleBox.setAlignment(Pos.CENTER_LEFT);

        btnBuildTree = new Button("Build Tree");
        btnBuildTree.getStyleClass().add("predict-button");
        btnBuildTree.setOnAction(e -> {
            if (dataLoaded) {
                buildDecisionTree();
            } else {
                showAlert(Alert.AlertType.ERROR, "Data Not Loaded", "Failed loading data.");
            }
        });

        HBox buttonBox = new HBox(20, dataOutputToggleBox, metricToggleBox, btnBuildTree);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10));
        buttonBox.getStyleClass().add("toggle-box");

        TabPane tabPane = new TabPane();
        tabPane.setPrefHeight(300);

        Tab gainTab = new Tab("Gain");
        gainTab.setClosable(false);
        gainTab.setContent(createGainTable());

        Tab entropyTab = new Tab("Entropy");
        entropyTab.setClosable(false);
        entropyTab.setContent(createEntropyTable());

        tabPane.getTabs().addAll(gainTab, entropyTab);

        VBox metricsBox = createPerformanceMetrics();

        VBox rightVBox = new VBox(20, tabPane, metricsBox);
        rightVBox.setPadding(new Insets(10));
        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.setPrefWidth(800);
        rightVBox.getStyleClass().add("right-vbox");
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(leftVBox);
        mainLayout.setCenter(rightVBox);
        mainLayout.setTop(buttonBox);
        mainLayout.getStyleClass().add("main-layout");

        Scene scene = new Scene(mainLayout, 1100, 700);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Mushroom Decision Tree UI");
        primaryStage.show();
    }

    private void buildDecisionTree() {
        btnBuildTree.setDisable(true);

        boolean useGainRatio = rbGainRatio.isSelected();
        builtDecisionTree = new DecisionTree();

        gainTable.getItems().clear();
        entropyTable.getItems().clear();
        clearMetrics();

        builtDecisionTree.buildTreeWithMetrics(
                trainingData,
                new ArrayList<>(ALL_ATTRIBUTES),
                useGainRatio,
                (splitPhase, metrics) -> Platform.runLater(() -> {
                    for (Map.Entry<String, Double> gainEntry : metrics.getGains().entrySet()) {
                        gainTable.getItems().add(new AbstractMap.SimpleEntry<>(
                                gainEntry.getKey() + " (Split " + splitPhase + ")",
                                gainEntry.getValue()
                        ));
                    }
                    for (Map.Entry<String, Double> entropyEntry : metrics.getEntropies().entrySet()) {
                        entropyTable.getItems().add(new AbstractMap.SimpleEntry<>(
                                entropyEntry.getKey() + " (Split " + splitPhase + ")",
                                entropyEntry.getValue()
                        ));
                    }
                })
        );

        decisionTreeArea.setText(builtDecisionTree.toString());

        calculateAndDisplayMetrics();

        btnBuildTree.setDisable(false);

        showAlert(Alert.AlertType.INFORMATION, "Tree Built", "Decision tree has been built successfully.");
    }

    private void calculateAndDisplayMetrics() {
        List<Mushroom> selectedData = rbTrainingOutput.isSelected() ? trainingData : testData;

        AccuracyMetrics.Results results = AccuracyMetrics.evaluate(builtDecisionTree, selectedData);

        txtAccuracy.setText(String.format("%.2f%%", results.accuracy * 100));
        txtPrecision.setText(String.format("%.2f%%", results.precision * 100));
        txtRecall.setText(String.format("%.2f%%", results.recall * 100));
        txtF1Score.setText(String.format("%.2f%%", results.f1 * 100));
    }

    private void clearMetrics() {
        txtAccuracy.clear();
        txtPrecision.clear();
        txtRecall.clear();
        txtF1Score.clear();
    }

    private TableView<Map.Entry<String, Double>> createGainTable() {
        gainTable = new TableView<>();
        gainTable.setPlaceholder(new Label("No Gain data available"));

        TableColumn<Map.Entry<String, Double>, String> colGainAttr = new TableColumn<>("Attribute (Split Phase)");
        colGainAttr.setMinWidth(200);
        colGainAttr.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        colGainAttr.setStyle("-fx-alignment: CENTER_LEFT; -fx-font-weight: bold;");

        TableColumn<Map.Entry<String, Double>, Double> colGain = new TableColumn<>("Gain");
        colGain.setMinWidth(100);
        colGain.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getValue()).asObject());
        colGain.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");

        gainTable.getColumns().addAll(colGainAttr, colGain);
        gainTable.setPrefWidth(400);
        gainTable.setPrefHeight(300);

        return gainTable;
    }

    private TableView<Map.Entry<String, Double>> createEntropyTable() {
        entropyTable = new TableView<>();
        entropyTable.setPlaceholder(new Label("No Entropy data available"));

        TableColumn<Map.Entry<String, Double>, String> colEntAttr = new TableColumn<>("Attribute (Split Phase)");
        colEntAttr.setMinWidth(200);
        colEntAttr.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        colEntAttr.setStyle("-fx-alignment: CENTER_LEFT; -fx-font-weight: bold;");

        TableColumn<Map.Entry<String, Double>, Double> colEntropy = new TableColumn<>("Entropy");
        colEntropy.setMinWidth(100);
        colEntropy.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getValue()).asObject());
        colEntropy.setStyle("-fx-alignment: CENTER_RIGHT; -fx-font-weight: bold;");

        entropyTable.getColumns().addAll(colEntAttr, colEntropy);
        entropyTable.setPrefWidth(400);
        entropyTable.setPrefHeight(300);

        return entropyTable;
    }

    private VBox createPerformanceMetrics() {
        Label lblMetrics = new Label("Performance Metrics");
        lblMetrics.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        txtAccuracy = new TextField();
        txtPrecision = new TextField();
        txtRecall = new TextField();
        txtF1Score = new TextField();
        txtAccuracy.setEditable(false);
        txtPrecision.setEditable(false);
        txtRecall.setEditable(false);
        txtF1Score.setEditable(false);

        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(10);
        metricsGrid.setVgap(10);
        metricsGrid.setPadding(new Insets(10));

        metricsGrid.add(new Label("Accuracy:"), 0, 0);
        metricsGrid.add(txtAccuracy, 1, 0);
        metricsGrid.add(new Label("Precision:"), 0, 1);
        metricsGrid.add(txtPrecision, 1, 1);
        metricsGrid.add(new Label("Recall:"), 0, 2);
        metricsGrid.add(txtRecall, 1, 2);
        metricsGrid.add(new Label("F1-Score:"), 0, 3);
        metricsGrid.add(txtF1Score, 1, 3);

        VBox metricsBox = new VBox(10, lblMetrics, metricsGrid);
        metricsBox.setAlignment(Pos.TOP_LEFT);
        metricsBox.setPadding(new Insets(10));
        metricsBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-border-width: 1;");
        metricsBox.setPrefWidth(400);
        metricsBox.setPrefHeight(200);

        return metricsBox;
    }

    private boolean loadData() {
        try {
            dataSet.loadData(Paths.get(getClass().getResource("/mushroom.csv").toURI()));
            dataSet.splitTrainingTest(0.1);
            trainingData = dataSet.getTrainingData();
            testData = dataSet.getTestData();
            return true;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Data Loading Error", "Failed to load data.");
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type, msg);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private VBox createTrainingSceneContent() {
        Label lblTables = new Label("Metrics Tables");
        lblTables.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        HBox tablesBox = new HBox(20, gainTable, entropyTable);
        tablesBox.setAlignment(Pos.CENTER);
        tablesBox.setPadding(new Insets(10));

        VBox trainingContent = new VBox(10, lblTables, tablesBox);
        trainingContent.setAlignment(Pos.TOP_CENTER);
        trainingContent.setPadding(new Insets(10));

        return trainingContent;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
