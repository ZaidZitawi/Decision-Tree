package com.example.decisiontree.Metrics;

import com.example.decisiontree.DataSet.Mushroom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GainCalculator {

    public static double calculateInfoGain(List<Mushroom> data, String attribute) {
        double parentEntropy = EntropyCalculator.calculateTargetEntropy(data);
        HashMap<String, Double> attributeEntropies = EntropyCalculator.calculateAttributeEntropies(data, attribute);
        double weightedEntropySum = calculateWeightedEntropySum(data, attribute, attributeEntropies);
        return parentEntropy - weightedEntropySum;
    }

    private static double calculateWeightedEntropySum(List<Mushroom> data, String attribute, HashMap<String, Double> attributeEntropies) {
        Map<String, List<Mushroom>> partitions = Splitter.partitionByAttribute(data, attribute);
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
        Map<String, List<Mushroom>> partitions = Splitter.partitionByAttribute(data, attribute);
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
}
