package com.example.decisiontree.Tree;

import com.example.decisiontree.DataSet.Mushroom;
import com.example.decisiontree.Metrics.InformationGainCalculator; // <-- Use your metrics
import com.example.decisiontree.Metrics.EntropyCalculator;         // If you need direct entropy usage

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages the building of a decision tree and provides a predict() method.
 */
public class DecisionTree {

    private TreeNode root;  // The root node of our decision tree
    // We can define a max depth as a constant or pass it via constructor
    private static final int MAX_DEPTH = 7;

    /**
     * Build the tree from training data.
     *
     * @param data           The training records (70% split).
     * @param attributes     The list of attribute names to consider splitting on
     *                       (e.g., CAP-SHAPE, ODOR, etc.).
     * @param useGainRatio   If true, use gain ratio. If false, use info gain.
     */
    public void buildTree(List<Mushroom> data, List<String> attributes, boolean useGainRatio) {
        // Start recursion at depth = 0
        this.root = buildRecursive(data, attributes, useGainRatio, 0);
    }

    /**
     * Recursively builds the tree with depth tracking.
     */
    private TreeNode buildRecursive(
            List<Mushroom> data,
            List<String> attributes,
            boolean useGainRatio,
            int currentDepth
    ) {
        // Base case 1: If all records are EDIBLE, return leaf node with label "EDIBLE".
        if (allEdible(data)) {
            return new TreeNode("EDIBLE");
        }
        // Base case 2: If all records are POISONOUS, return leaf node with label "POISONOUS".
        if (allPoisonous(data)) {
            return new TreeNode("POISONOUS");
        }
        // Base case 3: If no more attributes to split on OR we've reached max depth, pick majority label.
        if (attributes.isEmpty() || currentDepth >= MAX_DEPTH) {
            return new TreeNode(majorityLabel(data));
        }

        // Otherwise, pick the best attribute using Info Gain or Gain Ratio
        String bestAttribute = pickBestAttribute(data, attributes, useGainRatio);

        // If for some reason we can't find a best attribute, fallback to leaf
        if (bestAttribute == null) {
            return new TreeNode(majorityLabel(data));
        }

        // Create an internal node with that attribute
        TreeNode node = new TreeNode(null); // Not a leaf, so label is null
        node.setSplittingAttribute(bestAttribute);

        // Partition data based on the best attribute
        Map<String, List<Mushroom>> partitions = partitionData(data, bestAttribute);

        // Remove the chosen attribute from the list for child splits
        List<String> remainingAttributes = attributes.stream()
                .filter(attr -> !attr.equals(bestAttribute))
                .collect(Collectors.toList());

        // For each value of that attribute, build a subtree
        for (Map.Entry<String, List<Mushroom>> entry : partitions.entrySet()) {
            String attributeValue = entry.getKey();
            List<Mushroom> subset = entry.getValue();

            if (subset.isEmpty()) {
                // If no records for this branch, create a leaf with majority label
                node.addChild(attributeValue, new TreeNode(majorityLabel(data)));
            } else {
                // Recursively build the subtree, increasing depth
                TreeNode child = buildRecursive(subset, remainingAttributes, useGainRatio, currentDepth + 1);
                node.addChild(attributeValue, child);
            }
        }

        return node;
    }

    // --- Helpers: allEdible, allPoisonous, majorityLabel, etc. ---

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

    /**
     * Select the attribute with the highest Info Gain or Gain Ratio.
     */
    private String pickBestAttribute(List<Mushroom> data, List<String> attributes, boolean useGainRatio) {
        double bestScore = Double.NEGATIVE_INFINITY;
        String bestAttribute = null;

        for (String attr : attributes) {
            double score;
            if (useGainRatio) {
                score = InformationGainCalculator.calculateGainRatio(data, attr);
            } else {
                score = InformationGainCalculator.calculateInfoGain(data, attr);
            }
            if (score > bestScore) {
                bestScore = score;
                bestAttribute = attr;
            }
        }

        return bestAttribute;
    }

    /**
     * Groups records by the given attribute’s value.
     */
    private Map<String, List<Mushroom>> partitionData(List<Mushroom> data, String attribute) {
        return data.stream().collect(
                Collectors.groupingBy(record -> getAttributeValue(record, attribute))
        );
    }

    /**
     * Reflection or if-else logic to fetch the attribute value from the record.
     * For example, if attribute = "ODOR", return record.getOdor().
     */
    private String getAttributeValue(Mushroom record, String attribute) {
        switch (attribute.toUpperCase()) {
            case "CAP-SHAPE": return record.getCapShape();
            case "CAP-SURFACE": return record.getCapSurface();
            case "CAP-COLOR": return record.getCapColor();
            case "BRUISES": return record.getBruises();
            case "ODOR": return record.getOdor();
            case "GILL-ATTACHMENT": return record.getGillAttachment();
            case "GILL-SPACING": return record.getGillSpacing();
            case "GILL-SIZE": return record.getGillSize();
            case "GILL-COLOR": return record.getGillColor();
            case "STALK-SHAPE": return record.getStalkShape();
            case "STALK-ROOT": return record.getStalkRoot();
            case "STALK-SURFACE-ABOVE-RING": return record.getStalkSurfaceAboveRing();
            case "STALK-SRFACE-UNDER-RING": return record.getStalkSurfaceBelowRing();
            case "STALK-COLOR-ABOVE-RING": return record.getStalkColorAboveRing();
            case "STALK-COLOR-BELOW-RING": return record.getStalkColorBelowRing();
            case "VEIL-TYPE": return record.getVeilType();
            case "VEIL-COLOR": return record.getVeilColor();
            case "RING-NUMBER": return record.getRingNumber();
            case "RING-TYPE": return record.getRingType();
            case "SPORE-PRINT-COLOR": return record.getSporePrintColor();
            case "POPULATION": return record.getPopulation();
            case "HABITAT": return record.getHabitat();
            default: return "";
        }
    }

    /**
     * Predict a single new mushroom record (from user input on the UI).
     */
    public String predict(Mushroom record) {
        // Start at root and traverse down the tree
        TreeNode currentNode = root;
        while (!currentNode.isLeaf()) {
            String attr = currentNode.getSplittingAttribute();
            String value = getAttributeValue(record, attr);

            // If the child doesn't exist (unseen attribute value), break & return majority or "unknown"
            if (!currentNode.getChildren().containsKey(value)) {
                return majorityLabelFallback();
            }
            currentNode = currentNode.getChildren().get(value);
        }
        return currentNode.getLabel();  // "EDIBLE" or "POISONOUS"
    }

    private String majorityLabelFallback() {
        // If we reach an unknown path, we can guess or default:
        return "EDIBLE"; // or "POISONOUS"
    }

    /**
     * Generates a string representation of the tree that you can display
     * in the UI's decisionTreeArea. This is a simple text-based approach.
     */
    public String toString() {
        return treeToString(root, 0);
    }

    private String treeToString(TreeNode node, int depth) {
        // If leaf, return label
        if (node.isLeaf()) {
            return indent(depth) + " -> " + node.getLabel() + "\n";
        }
        // Otherwise, for each child...
        StringBuilder sb = new StringBuilder(indent(depth) + "[Split on: " + node.getSplittingAttribute() + "]\n");
        for (Map.Entry<String, TreeNode> entry : node.getChildren().entrySet()) {
            sb.append(indent(depth + 1))
                    .append("Value = ")
                    .append(entry.getKey())
                    .append(":\n")
                    .append(treeToString(entry.getValue(), depth + 2));
        }
        return sb.toString();
    }

    private String indent(int depth) {
        // Indent with e.g. two spaces per depth
        return "  ".repeat(depth);
    }

    public TreeNode getRoot() {
        return root;
    }
}
