package com.example.decisiontree.Metrics;

import com.example.decisiontree.DataSet.Mushroom;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class to calculate entropy.
 */
public class EntropyCalculator {

    /**
     * Calculates the entropy of a given list of Mushroom data.
     *
     * Entropy formula: -Σ (p_i * log2(p_i)), where p_i is the probability of each class.
     *
     * Supports multi-class classification by calculating probabilities dynamically.
     */
    public static double calculateEntropy(List<Mushroom> data) {
        if (data.isEmpty()) {
            return 0.0;
        }

        long total = data.size();

        // Count occurrences of each class label (edible/poisonous or other labels)
        Map<String, Long> classCounts = data.stream()
                .collect(Collectors.groupingBy(
                        mushroom -> mushroom.isEdible() ? "EDIBLE" : "POISONOUS", // Adjust for custom labels
                        Collectors.counting()
                ));

        // Calculate entropy
        double entropy = 0.0;
        for (Map.Entry<String, Long> entry : classCounts.entrySet()) {
            double probability = (double) entry.getValue() / total;
            if (probability > 0) {
                entropy += probability * log2(probability);
            }
        }

        return -entropy;
    }

    private static double log2(double value) {
        return Math.log(value) / Math.log(2);
    }
}
