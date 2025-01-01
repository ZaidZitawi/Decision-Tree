package com.example.decisiontree.Metrics;

import com.example.decisiontree.DataSet.Mushroom;
import com.example.decisiontree.Tree.DecisionTree;

import java.util.List;

/**
 * Utility class to calculate accuracy, precision, recall, F1-score
 * after you run the decision tree on test data.
 */
public class AccuracyMetrics {

    /**
     * A small data structure to hold results of classification metrics.
     */
    public static class Results {
        public double accuracy;
        public double precision;
        public double recall;
        public double f1;

        @Override
        public String toString() {
            return String.format("Accuracy=%.2f, Precision=%.2f, Recall=%.2f, F1=%.2f",
                    accuracy, precision, recall, f1);
        }
    }

    /**
     * Evaluate the decision tree on a set of test data.
     * Returns an object with accuracy, precision, recall, f1.
     */
    public static Results evaluate(DecisionTree decisionTree, List<Mushroom> testData) {

        int truePos = 0;  // edible predicted edible
        int falsePos = 0; // poisonous predicted edible
        int trueNeg = 0;  // poisonous predicted poisonous
        int falseNeg = 0; // edible predicted poisonous

        for (Mushroom record : testData) {
            // The ground truth
            boolean isEdible = record.isEdible();
            // Predict label
            String prediction = decisionTree.predict(record);
            boolean predictedEdible = prediction.equalsIgnoreCase("EDIBLE");

            if (isEdible && predictedEdible) {
                truePos++;
            } else if (!isEdible && predictedEdible) {
                falsePos++;
            } else if (!isEdible && !predictedEdible) {
                trueNeg++;
            } else if (isEdible && !predictedEdible) {
                falseNeg++;
            }
        }

        // Now compute metrics
        Results results = new Results();
        int total = testData.size();

        // accuracy = (TP + TN) / total
        results.accuracy = (double) (truePos + trueNeg) / total;

        System.out.println("Accuracy: " + results.accuracy);

        // precision = TP / (TP + FP)
        double denomPrecision = (truePos + falsePos);
        results.precision = (denomPrecision == 0) ? 0 : (double) truePos / denomPrecision;

        System.out.println("Precision: " + results.precision);
        // recall = TP / (TP + FN)
        double denomRecall = (truePos + falseNeg);
        results.recall = (denomRecall == 0) ? 0 : (double) truePos / denomRecall;

        System.out.println("Recall: " + results.recall);
        // F1 = 2 * (precision * recall) / (precision + recall)
        double denomF1 = (results.precision + results.recall);
        results.f1 = (denomF1 == 0) ? 0 : 2 * (results.precision * results.recall) / denomF1;

        System.out.println("F1: " + results.f1);
        return results;
    }
}
