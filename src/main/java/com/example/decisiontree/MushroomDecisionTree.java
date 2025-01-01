package com.example.decisiontree;

import com.example.decisiontree.DataSet.Mushroom;
import com.example.decisiontree.DataSet.MushroomDataSet;
import com.example.decisiontree.Metrics.AccuracyMetrics;
import com.example.decisiontree.Tree.DecisionTree;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class MushroomDecisionTree extends Application {

    // ---------------------------------------
    // UI Fields
    // ---------------------------------------
    private TextArea decisionTreeArea;
    private TableView<AttributeMetric> attributeTable;

    private TextField txtAccuracy;
    private TextField txtPrecision;
    private TextField txtRecall;
    private TextField txtF1Score;

    private RadioButton rbInfoGain;
    private RadioButton rbGainRatio;
    private RadioButton rbTrainingOutput;
    private RadioButton rbTestingOutput;

    private final TextField[] featureFields = new TextField[22];
    private Label lblPredictionResult;

    // ---------------------------------------
    // Logic Fields
    // ---------------------------------------
    private MushroomDataSet dataSet;
    private DecisionTree decisionTree;

    private static final List<String> ALL_ATTRIBUTES = Arrays.asList(
            "CAP-SHAPE","CAP-SURFACE","CAP-COLOR","BRUISES","ODOR","GILL-ATTACHMENT","GILL-SPACING",
            "GILL-SIZE","GILL-COLOR","STALK-SHAPE","STALK-ROOT","STALK-SURFACE-ABOVE-RING",
            "STALK-SRFACE-UNDER-RING","STALK-COLOR-ABOVE-RING","STALK-COLOR-BELOW-RING",
            "VEIL-TYPE","VEIL-COLOR","RING-NUMBER","RING-TYPE","SPORE-PRINT-COLOR","POPULATION","HABITAT"
    );

    @Override
    public void start(Stage primaryStage) {
        // LEFT: Decision Tree Visualization
        decisionTreeArea = new TextArea();
        decisionTreeArea.setPromptText("Decision Tree Visualization...");
        decisionTreeArea.getStyleClass().add("decision-tree-area");
        decisionTreeArea.setWrapText(true);

        VBox leftVBox = new VBox(decisionTreeArea);
        leftVBox.getStyleClass().add("left-vbox");
        VBox.setVgrow(decisionTreeArea, Priority.ALWAYS);

        // Radio toggles for training vs. testing
        Label lblDataOutputToggle = new Label("Select Data for Metrics: ");
        ToggleGroup dataToggleGroup = new ToggleGroup();
        rbTrainingOutput = new RadioButton("Training Data");
        rbTestingOutput = new RadioButton("Testing Data");
        rbTrainingOutput.setToggleGroup(dataToggleGroup);
        rbTestingOutput.setToggleGroup(dataToggleGroup);
        rbTrainingOutput.setSelected(true);

        HBox dataOutputToggleBox = new HBox(10, lblDataOutputToggle, rbTrainingOutput, rbTestingOutput);
        dataOutputToggleBox.setPadding(new Insets(10));

        // Radio toggles for Info Gain vs. Gain Ratio
        Label lblToggle = new Label("Select Algorithm: ");
        ToggleGroup metricToggleGroup = new ToggleGroup();
        rbInfoGain = new RadioButton("Information Gain");
        rbGainRatio = new RadioButton("Gain Ratio");
        rbInfoGain.setToggleGroup(metricToggleGroup);
        rbGainRatio.setToggleGroup(metricToggleGroup);
        rbInfoGain.setSelected(true);

        HBox metricToggleBox = new HBox(10, lblToggle, rbInfoGain, rbGainRatio);
        metricToggleBox.getStyleClass().add("toggle-box");

        // Button to build tree
        Button btnBuildTree = new Button("Build Tree");
        btnBuildTree.setOnAction(e -> buildAndEvaluateTree());

        HBox buttonBox = new HBox(10, dataOutputToggleBox, metricToggleBox, btnBuildTree);
        buttonBox.setPadding(new Insets(10));

        // RIGHT: TabPane
        TabPane tabPane = new TabPane();
        Tab trainingTab = new Tab("Training Scene");
        Tab edibilityTab = new Tab("Check Edibility Scene");

        trainingTab.setContent(createTrainingSceneContent());
        edibilityTab.setContent(createEdibilitySceneContent());
        trainingTab.setClosable(false);
        edibilityTab.setClosable(false);

        tabPane.getTabs().addAll(trainingTab, edibilityTab);

        VBox rightVBox = new VBox(buttonBox, tabPane);
        rightVBox.getStyleClass().add("right-vbox");

        // MAIN LAYOUT
        HBox mainLayout = new HBox(leftVBox, rightVBox);
        mainLayout.getStyleClass().add("main-layout");
        HBox.setHgrow(leftVBox, Priority.ALWAYS);
        HBox.setHgrow(rightVBox, Priority.ALWAYS);

        Scene scene = new Scene(mainLayout, 1200, 700);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Decision Tree UI");
        primaryStage.show();

        // Load the data from CSV
        loadData();
    }

    // -------------------------------------------------------------
    //  CREATE TRAINING TAB
    // -------------------------------------------------------------
    private VBox createTrainingSceneContent() {
        // Table for attributes (optional demo)
        attributeTable = new TableView<>();
        TableColumn<AttributeMetric, String> colAttrName = new TableColumn<>("Attribute");
        colAttrName.setCellValueFactory(new PropertyValueFactory<>("attributeName"));

        TableColumn<AttributeMetric, Double> colEntropy = new TableColumn<>("Entropy");
        colEntropy.setCellValueFactory(new PropertyValueFactory<>("entropy"));

        TableColumn<AttributeMetric, Double> colGain = new TableColumn<>("Gain");
        colGain.setCellValueFactory(new PropertyValueFactory<>("gain"));

        attributeTable.getColumns().addAll(colAttrName, colEntropy, colGain);

        // Example placeholder data:
        attributeTable.setItems(FXCollections.observableArrayList(
                new AttributeMetric("cap-shape", 0.54, 0.12),
                new AttributeMetric("cap-color", 0.47, 0.15)
        ));

        // Accuracy fields
        Label lblAccuracy = new Label("Accuracy:");
        txtAccuracy = new TextField();
        txtAccuracy.setPromptText("Accuracy");

        Label lblPrecision = new Label("Precision:");
        txtPrecision = new TextField();
        txtPrecision.setPromptText("Precision");

        Label lblRecall = new Label("Recall:");
        txtRecall = new TextField();
        txtRecall.setPromptText("Recall");

        Label lblF1 = new Label("F1-Score:");
        txtF1Score = new TextField();
        txtF1Score.setPromptText("F1-Score");

        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(10);
        metricsGrid.setVgap(10);
        metricsGrid.add(lblAccuracy, 0, 0);
        metricsGrid.add(txtAccuracy, 1, 0);
        metricsGrid.add(lblPrecision, 0, 1);
        metricsGrid.add(txtPrecision, 1, 1);
        metricsGrid.add(lblRecall, 0, 2);
        metricsGrid.add(txtRecall, 1, 2);
        metricsGrid.add(lblF1, 0, 3);
        metricsGrid.add(txtF1Score, 1, 3);

        VBox trainingContent = new VBox(10, attributeTable, metricsGrid);
        trainingContent.setPadding(new Insets(10));
        return trainingContent;
    }

    // -------------------------------------------------------------
    //  CREATE EDIBILITY TAB
    // -------------------------------------------------------------
    private VBox createEdibilitySceneContent() {
        // All 22 features
        String[] featureNames = {
                "CAP-SHAPE","CAP-SURFACE","CAP-COLOR","BRUISES","ODOR","GILL-ATTACHMENT","GILL-SPACING",
                "GILL-SIZE","GILL-COLOR","STALK-SHAPE","STALK-ROOT","STALK-SURFACE-ABOVE-RING",
                "STALK-SRFACE-UNDER-RING","STALK-COLOR-ABOVE-RING","STALK-COLOR-BELOW-RING",
                "VEIL-TYPE","VEIL-COLOR","RING-NUMBER","RING-TYPE","SPORE-PRINT-COLOR","POPULATION","HABITAT"
        };

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);

        int rowIndex = 0;
        for (int i = 0; i < featureNames.length; i += 2) {
            Label label1 = new Label(featureNames[i] + ":");
            TextField field1 = new TextField();
            field1.setPromptText(featureNames[i]);
            featureFields[i] = field1;
            formGrid.add(label1, 0, rowIndex);
            formGrid.add(field1, 1, rowIndex);

            if (i + 1 < featureNames.length) {
                Label label2 = new Label(featureNames[i + 1] + ":");
                TextField field2 = new TextField();
                field2.setPromptText(featureNames[i + 1]);
                featureFields[i + 1] = field2;
                formGrid.add(label2, 2, rowIndex);
                formGrid.add(field2, 3, rowIndex);
            }
            rowIndex++;
        }

        Button btnPredict = new Button("Predict Edibility");
        btnPredict.getStyleClass().add("predict-button");
        lblPredictionResult = new Label("Result will appear here...");

        btnPredict.setOnAction(e -> {
            if (decisionTree == null) {
                lblPredictionResult.setText("Please build the tree first!");
                return;
            }
            Mushroom userRecord = buildRecordFromUserInput();
            String prediction = decisionTree.predict(userRecord);
            lblPredictionResult.setText("Prediction: " + prediction);
        });

        ScrollPane scrollPane = new ScrollPane(formGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        VBox checkEdibilityBox = new VBox(15, scrollPane, btnPredict, lblPredictionResult);
        checkEdibilityBox.setPadding(new Insets(10));
        return checkEdibilityBox;
    }

    // -------------------------------------------------------------
    //  LOAD DATA
    // -------------------------------------------------------------
    private void loadData() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("mushroom.csv")) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found in resources!");
            }
            Path tempFile = Files.createTempFile("mushroom", ".csv");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            dataSet = new MushroomDataSet();
            dataSet.loadData(tempFile);
            dataSet.splitTrainingTest(0.70); // 70% train, 30% test
            decisionTreeArea.setText("Data loaded successfully from 'mushroom.csv'!");
        } catch (Exception ex) {
            decisionTreeArea.setText("Error loading data: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // -------------------------------------------------------------
    //  BUILD & EVALUATE TREE
    // -------------------------------------------------------------
    private void buildAndEvaluateTree() {
        if (dataSet == null || dataSet.getTrainingData().isEmpty()) {
            decisionTreeArea.setText("No data loaded or training data is empty!");
            return;
        }

        boolean useGainRatio = rbGainRatio.isSelected();

        // Build Decision Tree
        decisionTree = new DecisionTree();
        decisionTree.buildTree(
                dataSet.getTrainingData(),
                ALL_ATTRIBUTES,
                useGainRatio
        );

        if (decisionTree == null) {
            lblPredictionResult.setText("Tree building failed!");
            return;
        }

        // Display the tree
        decisionTreeArea.setText(decisionTree.toString());

        // Evaluate on training or testing data
        List<Mushroom> evalData = rbTrainingOutput.isSelected()
                ? dataSet.getTrainingData()
                : dataSet.getTestData();

        // Calculate metrics
        AccuracyMetrics.Results results = AccuracyMetrics.evaluate(decisionTree, evalData);

        // Display metrics
        txtAccuracy.setText(String.format("%.2f", results.accuracy));
        txtPrecision.setText(String.format("%.2f", results.precision));
        txtRecall.setText(String.format("%.2f", results.recall));
        txtF1Score.setText(String.format("%.2f", results.f1));
    }

    /**
     * Build a Mushroom record from the 22 text fields.
     * We pass 'false' for isEdible since we don't know and just want to predict.
     */
    private Mushroom buildRecordFromUserInput() {
        return new Mushroom(
                false, // placeholder, we don't know if it's edible
                featureFields[0].getText(),
                featureFields[1].getText(),
                featureFields[2].getText(),
                featureFields[3].getText(),
                featureFields[4].getText(),
                featureFields[5].getText(),
                featureFields[6].getText(),
                featureFields[7].getText(),
                featureFields[8].getText(),
                featureFields[9].getText(),
                featureFields[10].getText(),
                featureFields[11].getText(),
                featureFields[12].getText(),
                featureFields[13].getText(),
                featureFields[14].getText(),
                featureFields[15].getText(),
                featureFields[16].getText(),
                featureFields[17].getText(),
                featureFields[18].getText(),
                featureFields[19].getText(),
                featureFields[20].getText(),
                featureFields[21].getText()
        );
    }

    // -------------------------------------------------------------
    //  TABLEVIEW DATA MODEL
    // -------------------------------------------------------------
    public static class AttributeMetric {
        private String attributeName;
        private double entropy;
        private double gain;

        public AttributeMetric(String attributeName, double entropy, double gain) {
            this.attributeName = attributeName;
            this.entropy = entropy;
            this.gain = gain;
        }

        public String getAttributeName() {
            return attributeName;
        }

        public void setAttributeName(String attributeName) {
            this.attributeName = attributeName;
        }

        public double getEntropy() {
            return entropy;
        }

        public void setEntropy(double entropy) {
            this.entropy = entropy;
        }

        public double getGain() {
            return gain;
        }

        public void setGain(double gain) {
            this.gain = gain;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
