package com.example.decisiontree.Metrics;

import com.example.decisiontree.DataSet.Mushroom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GainCalculator {

    public static double calculateInfoGain(List<Mushroom> data, String attribute) {
        double parentEntropy = EntropyCalculator.calculateTargetEntropy(data);
        HashMap<String, Double> attributeEntropies = EntropyCalculator.calculateAttributeEntropies(data, attribute);
        double weightedEntropySum = calculateWeightedEntropySum(data, attribute, attributeEntropies);
        return parentEntropy - weightedEntropySum;
    }

    private static double calculateWeightedEntropySum(List<Mushroom> data, String attribute, HashMap<String, Double> attributeEntropies) {
        Map<String, List<Mushroom>> partitions = partitionByAttribute(data, attribute);
        int totalRecords = data.size();
        double weightedSum = 0.0;
        for (Map.Entry<String, List<Mushroom>> entry : partitions.entrySet()) {
            String attributeValue = entry.getKey();
            List<Mushroom> subset = entry.getValue();
            int subsetSize = subset.size();
            double subsetEntropy = attributeEntropies.get(attributeValue);
            double weight = (double) subsetSize / totalRecords;
            weightedSum += weight * subsetEntropy;
        }
        return weightedSum;
    }

    public static double calculateGainRatio(List<Mushroom> data, String attribute) {
        double infoGain = calculateInfoGain(data, attribute);
        Map<String, List<Mushroom>> partitions = partitionByAttribute(data, attribute);
        long totalSize = data.size();
        double splitInfo = 0.0;
        for (Map.Entry<String, List<Mushroom>> entry : partitions.entrySet()) {
            double subsetSize = entry.getValue().size();
            if (subsetSize > 0) {
                double proportion = subsetSize / (double) totalSize;
                splitInfo += proportion * log2(proportion);
            }
        }
        splitInfo = -splitInfo;
        if (splitInfo == 0.0) {
            return 0.0;
        }
        return infoGain / splitInfo;
    }

    private static double log2(double value) {
        return Math.log(value) / Math.log(2);
    }

    private static Map<String, List<Mushroom>> partitionByAttribute(List<Mushroom> data, String attribute) {
        return data.stream()
                .collect(Collectors.groupingBy(record -> getAttributeValue(record, attribute)));
    }

    private static String getAttributeValue(Mushroom record, String attribute) {
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
}
