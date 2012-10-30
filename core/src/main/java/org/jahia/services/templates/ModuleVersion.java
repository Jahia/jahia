package org.jahia.services.templates;

import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ModuleVersion implements Comparable<ModuleVersion> {
    private String versionString;

    private boolean isSnapshot;
    private List<Integer> orderedVersionNumbers = new ArrayList<Integer>();

    public ModuleVersion(String versionString) {
        this.versionString = versionString;
        if (versionString.endsWith("SNAPSHOT")) {
            isSnapshot = true;
        }
        String[] numbers = versionString.split("[^0-9]+");
        for (String number : numbers) {
            orderedVersionNumbers.add(Integer.parseInt(number));
        }
    }

    public boolean isSnapshot() {
        return isSnapshot;
    }

    public List<Integer> getOrderedVersionNumbers() {
        return orderedVersionNumbers;
    }

    public int compareTo(ModuleVersion o) {
        for (int i = 0; i < Math.min(orderedVersionNumbers.size(), o.orderedVersionNumbers.size()); i++) {
            int c = orderedVersionNumbers.get(i).compareTo(o.getOrderedVersionNumbers().get(i));
            if (c != 0) return c;
        }
        return new Integer(orderedVersionNumbers.size()).compareTo(o.orderedVersionNumbers.size());
    }

    @Override
    public String toString() {
        return versionString;
    }
}
