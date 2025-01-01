package com.example.decisiontree.DataSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MushroomDataSet {

    private final List<Mushroom> fullData = new ArrayList<>();
    private List<Mushroom> trainingData = new ArrayList<>();
    private List<Mushroom> testData = new ArrayList<>();

    /**
     * Reads the file from the provided path, parses each row into a Mushroom.
     * Adjust the parsing logic as needed to match your file format.
     */
    public void loadData(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String headerLine = reader.readLine();
            // e.g., "EDIBLE\tCAP-SHAPE\tCAP-SURFACE\tCAP-COLOR\t..."
            // You can store this header or ignore it if you know the columns already.

            String line;
            while ((line = reader.readLine()) != null) {
                // Example: 
                // "EDIBLE CONVEX SMOOTH WHITE BRUISES ALMOND FREE CROWDED NARROW WHITE TAPERING ..."
                // If your data is tab-separated, use split("\t"). If it's CSV, use split(",")
                String[] tokens = line.split(",");

                // This example assumes the first column is "EDIBLE" or "POISONOUS".
                // Then each subsequent column corresponds to the fields in the order described.
                // You may need to adjust indexes if your file layout is different.
                // Example data row (indexing them):
                // [0] -> EDIBLE or POISONOUS
                // [1] -> CAP-SHAPE
                // [2] -> CAP-SURFACE
                // [3] -> CAP-COLOR
                // [4] -> BRUISES
                // [5] -> ODOR
                // [6] -> GILL-ATTACHMENT
                // [7] -> GILL-SPACING
                // [8] -> GILL-SIZE
                // [9] -> GILL-COLOR
                // [10] -> STALK-SHAPE
                // [11] -> STALK-ROOT
                // [12] -> STALK-SURFACE-ABOVE-RING
                // [13] -> STALK-SRFACE-UNDER-RING
                // [14] -> STALK-COLOR-ABOVE-RING
                // [15] -> STALK-COLOR-BELOW-RING
                // [16] -> VEIL-TYPE
                // [17] -> VEIL-COLOR
                // [18] -> RING-NUMBER
                // [19] -> RING-TYPE
                // [20] -> SPORE-PRINT-COLOR
                // [21] -> POPULATION
                // [22] -> HABITAT
                // Some versions of the data have an index column at the start, so watch out for that.
                // If your data has an extra ID at [0], shift everything by 1.

                if (tokens.length < 23) {
                    // Possibly skip malformed lines or handle differently
                    continue;
                }

                boolean isEdible = tokens[0].equalsIgnoreCase("EDIBLE");

                Mushroom record = new Mushroom(
                        isEdible,                  // EDIBLE
                        tokens[1],                // CAP-SHAPE
                        tokens[2],                // CAP-SURFACE
                        tokens[3],                // CAP-COLOR
                        tokens[4],                // BRUISES
                        tokens[5],                // ODOR
                        tokens[6],                // GILL-ATTACHMENT
                        tokens[7],                // GILL-SPACING
                        tokens[8],                // GILL-SIZE
                        tokens[9],                // GILL-COLOR
                        tokens[10],               // STALK-SHAPE
                        tokens[11],               // STALK-ROOT
                        tokens[12],               // STALK-SURFACE-ABOVE-RING
                        tokens[13],               // STALK-SRFACE-UNDER-RING
                        tokens[14],               // STALK-COLOR-ABOVE-RING
                        tokens[15],               // STALK-COLOR-BELOW-RING
                        tokens[16],               // VEIL-TYPE
                        tokens[17],               // VEIL-COLOR
                        tokens[18],               // RING-NUMBER
                        tokens[19],               // RING-TYPE
                        tokens[20],               // SPORE-PRINT-COLOR
                        tokens[21],               // POPULATION
                        tokens[22]                // HABITAT
                );
                fullData.add(record);
            }
        }
    }

    /**
     * Splits the fullData list into 70% training and 30% test data.
     * Call this after loadData(), or re-call if you want a fresh shuffle & split.
     */
    public void splitTrainingTest(double trainRatio) {
        Collections.shuffle(fullData);
        int trainSize = (int) (fullData.size() * trainRatio);
        trainingData = fullData.subList(0, trainSize);
        testData = fullData.subList(trainSize, fullData.size());
    }

    /**
     * Convenience method to do a 70/30 split.
     */
    public void splitTrainingTest() {
        splitTrainingTest(0.60);
    }

    public List<Mushroom> getTrainingData() {
        return trainingData;
    }

    public List<Mushroom> getTestData() {
        return testData;
    }

    public List<Mushroom> getFullData() {
        return fullData;
    }
}
