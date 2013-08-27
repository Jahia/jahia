package org.jahia.modules.rolesmanager;

import java.io.Serializable;

public class NodeType implements Comparable<NodeType>, Serializable {
    private String name;
    private String displayName;
    private boolean set;

    public NodeType(String name, String displayName, boolean set) {
        this.name = name;
        this.displayName = displayName;
        this.set = set;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setSet(boolean set) {
        this.set = set;
    }

    public boolean isSet() {
        return set;
    }

    @Override
    public int compareTo(NodeType o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }
}
