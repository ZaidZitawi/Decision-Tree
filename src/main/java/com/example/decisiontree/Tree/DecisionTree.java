package com.example.decisiontree.Tree;

import com.example.decisiontree.DataSet.Mushroom;
import com.example.decisiontree.Metrics.GainCalculator;
import com.example.decisiontree.Metrics.EntropyCalculator;

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

    public void buildTree(List<Mushroom> data, List<String> attributes, boolean useGainRatio) {
        this.root = buildRecursive(data, attributes, useGainRatio, 0, null);
    }

    public void buildTreeWithMetrics(
            List<Mushroom> data,
            List<String> attributes,
            boolean useGainRatio,
            BiConsumer<Integer, SplitMetrics> metricsCallback
    ) {
        this.root = buildRecursive(data, attributes, useGainRatio, 0, metricsCallback);
    }

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

        Map<String, Double> attributeGains = new HashMap<>();
        for (String attribute : attributes) {
            double gain = useGainRatio
                    ? GainCalculator.calculateGainRatio(data, attribute)
                    : GainCalculator.calculateInfoGain(data, attribute);
            attributeGains.put(attribute, gain);
        }

        Map<String, Double> attributeEntropies = new HashMap<>();
        for (String attribute : attributes) {
            Map<String, Double> entropies = EntropyCalculator.calculateAttributeEntropies(data, attribute);
            double averageEntropy = entropies.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            attributeEntropies.put(attribute, averageEntropy);
        }

        if (metricsCallback != null) {
            int splitPhase = currentDepth + 1;
            SplitMetrics metrics = new SplitMetrics(attributeGains, attributeEntropies);
            metricsCallback.accept(splitPhase, metrics);
        }

        String bestAttribute = selectBestAttribute(attributeGains);

        if (bestAttribute == null) {
            return new TreeNode(majorityLabel(data));
        }

        TreeNode node = new TreeNode(null);
        node.setSplittingAttribute(bestAttribute);

        Map<String, List<Mushroom>> partitions = partitionData(data, bestAttribute);

        List<String> remainingAttributes = attributes.stream()
                .filter(attr -> !attr.equals(bestAttribute))
                .collect(Collectors.toList());

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

    private String selectBestAttribute(Map<String, Double> attributeGains) {
        return attributeGains.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);
    }

    private Map<String, List<Mushroom>> partitionData(List<Mushroom> data, String attribute) {
        return data.stream().collect(
                Collectors.groupingBy(record -> getAttributeValue(record, attribute))
        );
    }

    private String getAttributeValue(Mushroom record, String attribute) {
        switch (attribute.toUpperCase()) {
            case "CAP-SHAPE":
                return record.getCapShape();
            case "CAP-SURFACE":
                return record.getCapSurface();
            case "CAP-COLOR":
                return record.getCapColor();
            case "BRUISES":
                return record.getBruises();
            case "ODOR":
                return record.getOdor();
            case "GILL-ATTACHMENT":
                return record.getGillAttachment();
            case "GILL-SPACING":
                return record.getGillSpacing();
            case "GILL-SIZE":
                return record.getGillSize();
            case "GILL-COLOR":
                return record.getGillColor();
            case "STALK-SHAPE":
                return record.getStalkShape();
            case "STALK-ROOT":
                return record.getStalkRoot();
            case "STALK-SURFACE-ABOVE-RING":
                return record.getStalkSurfaceAboveRing();
            case "STALK-SRFACE-UNDER-RING":
                return record.getStalkSurfaceBelowRing();
            case "STALK-COLOR-ABOVE-RING":
                return record.getStalkColorAboveRing();
            case "STALK-COLOR-BELOW-RING":
                return record.getStalkColorBelowRing();
            case "VEIL-TYPE":
                return record.getVeilType();
            case "VEIL-COLOR":
                return record.getVeilColor();
            case "RING-NUMBER":
                return record.getRingNumber();
            case "RING-TYPE":
                return record.getRingType();
            case "SPORE-PRINT-COLOR":
                return record.getSporePrintColor();
            case "POPULATION":
                return record.getPopulation();
            case "HABITAT":
                return record.getHabitat();
            default:
                return "";
        }
    }

    private boolean allEdible(List<Mushroom> data) {
        return data.stream().allMatch(Mushroom::isEdible);
    }

    private boolean allPoisonous(List<Mushroom> data) {
        return data.stream().noneMatch(Mushroom::isEdible);
    }

    private String majorityLabel(List<Mushroom> data) {
        long edibleCount = data.stream().filter(Mushroom::isEdible).count();
        long poisonousCount = data.size() - edibleCount;
        return (edibleCount >= poisonousCount) ? "EDIBLE" : "POISONOUS";
    }

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

    private String majorityLabelFallback() {
        return "EDIBLE";
    }

    @Override
    public String toString() {
        return treeToString(root, "", true);
    }

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

    public TreeNode getRoot() {
        return root;
    }
}
