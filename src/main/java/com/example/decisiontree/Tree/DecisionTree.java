package com.example.decisiontree.Tree;

import com.example.decisiontree.DataSet.Mushroom;
import com.example.decisiontree.Metrics.GainCalculator;
import com.example.decisiontree.Metrics.EntropyCalculator;
import com.example.decisiontree.Metrics.Splitter;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class DecisionTree {

    private TreeNode root;
    private static final int MAX_DEPTH = 7;

    public static class SplitMetrics {
        private Map<String, Double> gains;
        private Map<String, Double> entropies;

        public SplitMetrics(Map<String, Double> gains, Map<String, Double> entropies) {
            this.gains = gains;
            this.entropies = entropies;
        }

        public Map<String, Double> getGains() {
            return gains;
        }

        public Map<String, Double> getEntropies() {
            return entropies;
        }
    }

    // Builds the decision tree using the provided data and attributes
    public void buildTree(List<Mushroom> data, List<String> attributes, boolean useGainRatio) {
        this.root = buildRecursive(data, attributes, useGainRatio, 0, null);
    }

    // Builds the decision tree while capturing gain and entropy metrics
    public void buildTreeWithMetrics(
            List<Mushroom> data,
            List<String> attributes,
            boolean useGainRatio,
            BiConsumer<Integer, SplitMetrics> metricsCallback
    ) {
        this.root = buildRecursive(data, attributes, useGainRatio, 0, metricsCallback);
    }

    // Recursively constructs the decision tree
    private TreeNode buildRecursive(
            List<Mushroom> data,
            List<String> attributes,
            boolean useGainRatio,
            int currentDepth,
            BiConsumer<Integer, SplitMetrics> metricsCallback
    ) {
        if (allEdible(data)) {
            return new TreeNode("EDIBLE");
        }
        if (allPoisonous(data)) {
            return new TreeNode("POISONOUS");
        }
        if (attributes.isEmpty() || currentDepth >= MAX_DEPTH) {
            return new TreeNode(majorityLabel(data));
        }

        // Calculate gains for all attributes
        Map<String, Double> attributeGains = new HashMap<>();
        for (String attribute : attributes) {
            double gain = useGainRatio
                    ? GainCalculator.calculateGainRatio(data, attribute)
                    : GainCalculator.calculateInfoGain(data, attribute);
            attributeGains.put(attribute, gain);
        }

        // Calculate entropies for all attributes
        Map<String, Double> attributeEntropies = new HashMap<>();
        for (String attribute : attributes) {
            Map<String, Double> entropies = EntropyCalculator.calculateAttributeEntropies(data, attribute);
            double averageEntropy = entropies.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            attributeEntropies.put(attribute, averageEntropy);
        }

        // Capture metrics if callback is provided
        if (metricsCallback != null) {
            int splitPhase = currentDepth + 1;
            SplitMetrics metrics = new SplitMetrics(attributeGains, attributeEntropies);
            metricsCallback.accept(splitPhase, metrics);
        }

        // Select the best attribute to split on
        String bestAttribute = selectBestAttribute(attributeGains);

        if (bestAttribute == null) {
            return new TreeNode(majorityLabel(data));
        }

        TreeNode node = new TreeNode(null);
        node.setSplittingAttribute(bestAttribute);

        // Partition data based on the best attribute
        Map<String, List<Mushroom>> partitions = Splitter.partitionByAttribute(data, bestAttribute);

        // Prepare remaining attributes for child nodes
        List<String> remainingAttributes = attributes.stream()
                .filter(attr -> !attr.equals(bestAttribute))
                .collect(Collectors.toList());

        // Recursively build child nodes
        for (Map.Entry<String, List<Mushroom>> entry : partitions.entrySet()) {
            String attributeValue = entry.getKey();
            List<Mushroom> subset = entry.getValue();

            if (subset.isEmpty()) {
                node.addChild(attributeValue, new TreeNode(majorityLabel(data)));
            } else {
                TreeNode child = buildRecursive(subset, remainingAttributes, useGainRatio, currentDepth + 1, metricsCallback);
                node.addChild(attributeValue, child);
            }
        }

        return node;
    }

    // Selects the attribute with the highest gain
    private String selectBestAttribute(Map<String, Double> attributeGains) {
        return attributeGains.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);
    }

    // Partitions the data based on the specified attribute
    private Map<String, List<Mushroom>> partitionData(List<Mushroom> data, String attribute) {
        return Splitter.partitionByAttribute(data, attribute);
    }

    // Retrieves the value of a specific attribute from a Mushroom instance
    private String getAttributeValue(Mushroom record, String attribute) {
        return Splitter.getAttributeValue(record, attribute);
    }

    // Checks if all records are edible
    private boolean allEdible(List<Mushroom> data) {
        return data.stream().allMatch(Mushroom::isEdible);
    }

    // Checks if all records are poisonous
    private boolean allPoisonous(List<Mushroom> data) {
        return data.stream().noneMatch(Mushroom::isEdible);
    }

    // Determines the majority label in the data
    private String majorityLabel(List<Mushroom> data) {
        long edibleCount = data.stream().filter(Mushroom::isEdible).count();
        long poisonousCount = data.size() - edibleCount;
        return (edibleCount >= poisonousCount) ? "EDIBLE" : "POISONOUS";
    }

    // Predicts the label ("EDIBLE" or "POISONOUS") for a given Mushroom record
    public String predict(Mushroom record) {
        TreeNode currentNode = root;
        while (!currentNode.isLeaf()) {
            String attr = currentNode.getSplittingAttribute();
            String value = getAttributeValue(record, attr);

            if (!currentNode.getChildren().containsKey(value)) {
                return majorityLabelFallback();
            }
            currentNode = currentNode.getChildren().get(value);
        }
        return currentNode.getLabel();
    }

    // Fallback method to return a default label if traversal fails
    private String majorityLabelFallback() {
        return "EDIBLE";
    }

    // Generates a string representation of the decision tree
    @Override
    public String toString() {
        return treeToString(root, "", true);
    }

    // Recursively builds the string representation of the tree
    private String treeToString(TreeNode node, String prefix, boolean isTail) {
        StringBuilder sb = new StringBuilder();

        if (node.isLeaf()) {
            sb.append(prefix).append(isTail ? "└── " : "├── ").append("Leaf: ").append(node.getLabel()).append("\n");
        } else {
            sb.append(prefix).append(isTail ? "└── " : "├── ").append("[Split on: ").append(node.getSplittingAttribute()).append("]\n");
            List<Map.Entry<String, TreeNode>> children = node.getChildrenList();

            for (int i = 0; i < children.size(); i++) {
                Map.Entry<String, TreeNode> entry = children.get(i);
                boolean last = (i == children.size() - 1);
                String newPrefix = prefix + (isTail ? "    " : "│   ");
                sb.append(newPrefix)
                        .append(last ? "└── " : "├── ")
                        .append("Value = ").append(entry.getKey()).append(":\n");
                sb.append(treeToString(entry.getValue(), newPrefix + (last ? "    " : "│   "), true));
            }
        }

        return sb.toString();
    }

    // Retrieves the root of the decision tree
    public TreeNode getRoot() {
        return root;
    }

    // Prunes the tree using post-pruning with the provided validation data
    public void prune(List<Mushroom> validationData) {
        pruneRecursive(root, validationData);
    }

    // Recursively prunes the tree
    private void pruneRecursive(TreeNode node, List<Mushroom> validationData) {
        if (node.isLeaf()) {
            return;
        }

        // Traverse all children first
        for (TreeNode child : node.getChildren().values()) {
            pruneRecursive(child, validationData);
        }

        // Attempt to prune this node
        Map<String, List<Mushroom>> partitions = Splitter.partitionByAttribute(validationData, node.getSplittingAttribute());

        // Calculate current accuracy
        double currentAccuracy = calculateAccuracy(validationData);

        // Backup the current state
        String originalAttribute = node.getSplittingAttribute();
        Map<String, TreeNode> originalChildren = new HashMap<>(node.getChildren());
        node.setSplittingAttribute(null);
        node.setLabel(majorityLabel(validationData));

        // Calculate new accuracy after pruning
        double prunedAccuracy = calculateAccuracy(validationData);

        // Decide to keep pruning or revert
        if (prunedAccuracy < currentAccuracy) {
            // Revert pruning
            node.setSplittingAttribute(originalAttribute);
            node.setLabel(null);
            node.setChildren(originalChildren);
        }
    }

    // Calculates the accuracy of the tree on the given data
    private double calculateAccuracy(List<Mushroom> data) {
        int correct = 0;
        for (Mushroom m : data) {
            String prediction = predict(m);
            String actual = m.isEdible() ? "EDIBLE" : "POISONOUS";
            if (prediction.equals(actual)) {
                correct++;
            }
        }
        return data.isEmpty() ? 0.0 : (double) correct / data.size();
    }
}
