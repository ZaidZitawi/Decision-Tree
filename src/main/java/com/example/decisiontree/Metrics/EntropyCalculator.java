package com.example.decisiontree.Metrics;

import com.example.decisiontree.DataSet.Mushroom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntropyCalculator {

    public static double calculateTargetEntropy(List<Mushroom> data) {
        if (data.isEmpty()) {
            return 0.0;
        }

        long total = data.size();

        Map<String, Long> classCounts = data.stream()
                .collect(Collectors.groupingBy(
                        mushroom -> mushroom.isEdible() ? "EDIBLE" : "POISONOUS",
                        Collectors.counting()
                ));

        double entropy = 0.0;
        for (Map.Entry<String, Long> entry : classCounts.entrySet()) {
            double probability = (double) entry.getValue() / total;
            if (probability > 0) {
                entropy += probability * log2(probability);
            }
        }

        return -entropy;
    }

    public static HashMap<String, Double> calculateAttributeEntropies(List<Mushroom> data, String attribute) {
        HashMap<String, Double> attributeEntropies = new HashMap<>();

        if (data.isEmpty()) {
            return attributeEntropies;
        }

        Map<String, List<Mushroom>> partitions = partitionByAttribute(data, attribute);

        for (Map.Entry<String, List<Mushroom>> entry : partitions.entrySet()) {
            String attributeValue = entry.getKey();
            List<Mushroom> subset = entry.getValue();

            double entropy = calculateTargetEntropy(subset);
            attributeEntropies.put(attributeValue, entropy);
        }

        return attributeEntropies;
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

    private static double log2(double value) {
        return Math.log(value) / Math.log(2);
    }
}
