package com.example.decisiontree.Tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single node in the decision tree.
 */
public class TreeNode {

    // If this node is a leaf, store the classification here (e.g., "EDIBLE" or "POISONOUS").
    private String label;

    // If this node is not a leaf, store the attribute used for splitting .
    private String splittingAttribute;

    // Child nodes for each possible value of the splitting attribute (e.g., "CONVEX", "BELL", etc.).
    private Map<String, TreeNode> children = new HashMap<>();

    // Constructor for a leaf node
    public TreeNode(String label) {
        this.label = label; // e.g. "EDIBLE" or "POISONOUS"
    }

    // Optional constructor for an internal node
    public TreeNode(String splittingAttribute, Map<String, TreeNode> children) {
        this.splittingAttribute = splittingAttribute;
        this.children = children;
    }


    public String getLabel() {
        return label;
    }

    public boolean isLeaf() {
        return label != null;
    }

    public String getSplittingAttribute() {
        return splittingAttribute;
    }

    public void setSplittingAttribute(String splittingAttribute) {
        this.splittingAttribute = splittingAttribute;
    }

    public Map<String, TreeNode> getChildren() {
        return children;
    }

    public void setChildren(Map<String, TreeNode> children) {
        this.children = children;
    }


    public void addChild(String attributeValue, TreeNode childNode) {
        this.children.put(attributeValue, childNode);
    }

    public List<Map.Entry<String, TreeNode>> getChildrenList() {
        return new ArrayList<>(children.entrySet());
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
