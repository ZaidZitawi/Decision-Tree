package com.example.decisiontree.DataSet;

public class Mushroom {

    private final boolean edible;
    private final String capShape;
    private final String capSurface;
    private final String capColor;
    private final String bruises;
    private final String odor;
    private final String gillAttachment;
    private final String gillSpacing;
    private final String gillSize;
    private final String gillColor;
    private final String stalkShape;
    private final String stalkRoot;
    private final String stalkSurfaceAboveRing;
    private final String stalkSurfaceBelowRing;
    private final String stalkColorAboveRing;
    private final String stalkColorBelowRing;
    private final String veilType;
    private final String veilColor;
    private final String ringNumber;
    private final String ringType;
    private final String sporePrintColor;
    private final String population;
    private final String habitat;

    public Mushroom(boolean edible,
                          String capShape,
                          String capSurface,
                          String capColor,
                          String bruises,
                          String odor,
                          String gillAttachment,
                          String gillSpacing,
                          String gillSize,
                          String gillColor,
                          String stalkShape,
                          String stalkRoot,
                          String stalkSurfaceAboveRing,
                          String stalkSurfaceBelowRing,
                          String stalkColorAboveRing,
                          String stalkColorBelowRing,
                          String veilType,
                          String veilColor,
                          String ringNumber,
                          String ringType,
                          String sporePrintColor,
                          String population,
                          String habitat) {
        this.edible = edible;
        this.capShape = capShape;
        this.capSurface = capSurface;
        this.capColor = capColor;
        this.bruises = bruises;
        this.odor = odor;
        this.gillAttachment = gillAttachment;
        this.gillSpacing = gillSpacing;
        this.gillSize = gillSize;
        this.gillColor = gillColor;
        this.stalkShape = stalkShape;
        this.stalkRoot = stalkRoot;
        this.stalkSurfaceAboveRing = stalkSurfaceAboveRing;
        this.stalkSurfaceBelowRing = stalkSurfaceBelowRing;
        this.stalkColorAboveRing = stalkColorAboveRing;
        this.stalkColorBelowRing = stalkColorBelowRing;
        this.veilType = veilType;
        this.veilColor = veilColor;
        this.ringNumber = ringNumber;
        this.ringType = ringType;
        this.sporePrintColor = sporePrintColor;
        this.population = population;
        this.habitat = habitat;
    }

    // --- GETTERS ---
    public boolean isEdible() {
        return edible;
    }

    public String getCapShape() {
        return capShape;
    }

    public String getCapSurface() {
        return capSurface;
    }

    public String getCapColor() {
        return capColor;
    }

    public String getBruises() {
        return bruises;
    }

    public String getOdor() {
        return odor;
    }

    public String getGillAttachment() {
        return gillAttachment;
    }

    public String getGillSpacing() {
        return gillSpacing;
    }

    public String getGillSize() {
        return gillSize;
    }

    public String getGillColor() {
        return gillColor;
    }

    public String getStalkShape() {
        return stalkShape;
    }

    public String getStalkRoot() {
        return stalkRoot;
    }

    public String getStalkSurfaceAboveRing() {
        return stalkSurfaceAboveRing;
    }

    public String getStalkSurfaceBelowRing() {
        return stalkSurfaceBelowRing;
    }

    public String getStalkColorAboveRing() {
        return stalkColorAboveRing;
    }

    public String getStalkColorBelowRing() {
        return stalkColorBelowRing;
    }

    public String getVeilType() {
        return veilType;
    }

    public String getVeilColor() {
        return veilColor;
    }

    public String getRingNumber() {
        return ringNumber;
    }

    public String getRingType() {
        return ringType;
    }

    public String getSporePrintColor() {
        return sporePrintColor;
    }

    public String getPopulation() {
        return population;
    }

    public String getHabitat() {
        return habitat;
    }
}
