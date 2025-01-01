package com.example.decisiontree.Metrics;


import com.example.decisiontree.DataSet.Mushroom;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for calculating Gain Ratio
 */
public class GainRatioCalculator {

    /**
     * GainRatio = InformationGain / SplitInfo
     *
     * where SplitInfo = - Σ(|subset|/|parent| * log2(|subset|/|parent|))
     */
    public static double calculateGainRatio(List<Mushroom> data, String attribute) {
        double infoGain = InformationGainCalculator.calculateInfoGain(data, attribute);
        double splitInfo = calculateSplitInfo(data, attribute);
        if (splitInfo == 0.0) {
            // Avoid division by zero. Some prefer returning 0 if splitInfo=0.
            return 0.0;
        }
        return infoGain / splitInfo;
    }

    /**
     * SplitInfo = - Σ( (|subset| / total) * log2(|subset|/ total ) )
     */
    private static double calculateSplitInfo(List<Mushroom> data, String attribute) {
        Map<String, List<Mushroom>> partitions = partitionByAttribute(data, attribute);
        long total = data.size();

        double sum = 0.0;
        for (List<Mushroom> subset : partitions.values()) {
            double fraction = (double) subset.size() / total;
            if (fraction > 0) {
                sum += (fraction * (Math.log(fraction) / Math.log(2)));
            }
        }
        return -sum;
    }

    private static Map<String, List<Mushroom>> partitionByAttribute(List<Mushroom> data, String attribute) {
        return data.stream().collect(Collectors.groupingBy(record -> getAttributeValue(record, attribute)));
    }

    private static String getAttributeValue(Mushroom record, String attribute) {
        // Same approach as in InfoGain
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
