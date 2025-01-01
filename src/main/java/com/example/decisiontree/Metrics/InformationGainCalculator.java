package com.example.decisiontree.Metrics;

import com.example.decisiontree.DataSet.Mushroom;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class to calculate information gain for splitting on a particular attribute.
 */
public class InformationGainCalculator {

    /**
     * For a given list of data and a chosen attribute, we can calculate:
     *
     * InfoGain = Entropy(parent) - Σ( (|subset| / |parent|) * Entropy(subset) )
     */
    public static double calculateInfoGain(List<Mushroom> data, String attribute) {
        double parentEntropy = EntropyCalculator.calculateEntropy(data);

        // Partition data by attribute value
        Map<String, List<Mushroom>> partitions = partitionByAttribute(data, attribute);

        // Weighted sum of child entropies
        long totalSize = data.size();
        double weightedSum = 0.0;
        for (Map.Entry<String, List<Mushroom>> entry : partitions.entrySet()) {
            List<Mushroom> subset = entry.getValue();
            double subsetEntropy = EntropyCalculator.calculateEntropy(subset);
            weightedSum += ((double) subset.size() / totalSize) * subsetEntropy;
        }


        return parentEntropy - weightedSum;
    }

    /**
     * Gain Ratio = InfoGain / SplitInfo
     *
     * SplitInfo = -Σ( (|subset| / |parent|) * log2(|subset| / |parent|) )
     *
     * If splitInfo is zero (e.g. all data end up in the same branch), we return 0
     * to avoid division by zero.
     */
    public static double calculateGainRatio(List<Mushroom> data, String attribute) {
        double infoGain = calculateInfoGain(data, attribute);

        double splitInfo = 0.0;
        Map<String, List<Mushroom>> partitions = partitionByAttribute(data, attribute);
        long totalSize = data.size();

        for (Map.Entry<String, List<Mushroom>> entry : partitions.entrySet()) {
            double subsetSize = entry.getValue().size();
            if (subsetSize > 0) {
                double proportion = subsetSize / totalSize;
                splitInfo += proportion * log2(proportion);
            }
        }
        splitInfo = -splitInfo;

        if (splitInfo == 0.0) {
            // Avoid division by zero
            return 0.0;
        }

        return infoGain / splitInfo;
    }

    private static double log2(double value) {
        return Math.log(value) / Math.log(2);
    }

    /**
     * Partition the records based on the provided attribute's value.
     */
    private static Map<String, List<Mushroom>> partitionByAttribute(List<Mushroom> data, String attribute) {
        return data.stream()
                .collect(Collectors.groupingBy(record -> getAttributeValue(record, attribute)));
    }

    private static String getAttributeValue(Mushroom record, String attribute) {
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
}
