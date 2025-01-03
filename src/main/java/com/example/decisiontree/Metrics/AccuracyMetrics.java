package com.example.decisiontree.Metrics;

import com.example.decisiontree.DataSet.Mushroom;
import com.example.decisiontree.Tree.DecisionTree;

import java.util.List;

public class AccuracyMetrics {

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

    public static Results evaluate(DecisionTree decisionTree, List<Mushroom> testData) {

        int truePos = 0;
        int falsePos = 0;
        int trueNeg = 0;
        int falseNeg = 0;

        for (Mushroom record : testData) {
            boolean isEdible = record.isEdible();
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

        Results results = new Results();
        int total = testData.size();

        results.accuracy = (double) (truePos + trueNeg) / total;

        double denomPrecision = (truePos + falsePos);
        results.precision = (denomPrecision == 0) ? 0 : (double) truePos / denomPrecision;

        double denomRecall = (truePos + falseNeg);
        results.recall = (denomRecall == 0) ? 0 : (double) truePos / denomRecall;

        double denomF1 = (results.precision + results.recall);
        results.f1 = (denomF1 == 0) ? 0 : 2 * (results.precision * results.recall) / denomF1;

        return results;
    }
}
