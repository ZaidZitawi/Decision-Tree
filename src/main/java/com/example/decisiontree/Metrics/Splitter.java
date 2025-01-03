package com.example.decisiontree.Metrics;

import com.example.decisiontree.DataSet.Mushroom;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Splitter {

    public static Map<String, List<Mushroom>> partitionByAttribute(List<Mushroom> data, String attribute) {
        return data.stream()
                .collect(Collectors.groupingBy(record -> getAttributeValue(record, attribute)));
    }

    public static String getAttributeValue(Mushroom record, String attribute) {
        return switch (attribute.toUpperCase()) {
            case "CAP-SHAPE" -> record.getCapShape();
            case "CAP-SURFACE" -> record.getCapSurface();
            case "CAP-COLOR" -> record.getCapColor();
            case "BRUISES" -> record.getBruises();
            case "ODOR" -> record.getOdor();
            case "GILL-ATTACHMENT" -> record.getGillAttachment();
            case "GILL-SPACING" -> record.getGillSpacing();
            case "GILL-SIZE" -> record.getGillSize();
            case "GILL-COLOR" -> record.getGillColor();
            case "STALK-SHAPE" -> record.getStalkShape();
            case "STALK-ROOT" -> record.getStalkRoot();
            case "STALK-SURFACE-ABOVE-RING" -> record.getStalkSurfaceAboveRing();
            case "STALK-SRFACE-UNDER-RING" -> record.getStalkSurfaceBelowRing();
            case "STALK-COLOR-ABOVE-RING" -> record.getStalkColorAboveRing();
            case "STALK-COLOR-BELOW-RING" -> record.getStalkColorBelowRing();
            case "VEIL-TYPE" -> record.getVeilType();
            case "VEIL-COLOR" -> record.getVeilColor();
            case "RING-NUMBER" -> record.getRingNumber();
            case "RING-TYPE" -> record.getRingType();
            case "SPORE-PRINT-COLOR" -> record.getSporePrintColor();
            case "POPULATION" -> record.getPopulation();
            case "HABITAT" -> record.getHabitat();
            default -> "";
        };
    }
}
