package org.jahia.services.templates;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ModuleVersion implements Comparable<ModuleVersion> {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("[^0-9]+");

    private String versionString;

    private boolean isSnapshot;
    private List<Integer> orderedVersionNumbers = new ArrayList<Integer>();

    public ModuleVersion(String versionString) {
        this.versionString = versionString;
        if (versionString.endsWith("SNAPSHOT")) {
            isSnapshot = true;
        }
        String[] numbers = VERSION_PATTERN.split(versionString);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModuleVersion that = (ModuleVersion) o;

        if (versionString != null ? !versionString.equals(that.versionString) : that.versionString != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return versionString != null ? versionString.hashCode() : 0;
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
