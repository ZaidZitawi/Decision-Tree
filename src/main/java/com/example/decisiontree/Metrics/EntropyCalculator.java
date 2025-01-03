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

        Map<String, List<Mushroom>> partitions = Splitter.partitionByAttribute(data, attribute);

        for (Map.Entry<String, List<Mushroom>> entry : partitions.entrySet()) {
            String attributeValue = entry.getKey();
            List<Mushroom> subset = entry.getValue();

            double entropy = calculateTargetEntropy(subset);
            attributeEntropies.put(attributeValue, entropy);
        }

        return attributeEntropies;
    }

    private static double log2(double value) {
        return Math.log(value) / Math.log(2);
    }
}
