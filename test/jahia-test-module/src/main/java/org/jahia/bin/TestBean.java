package org.jahia.bin;

import junit.framework.TestCase;

import java.util.List;

/**
 * Simple bean that list all test cases
 */
public class TestBean implements Comparable {
    private List<String> testCases;
    private List<String> ignoredTests;
    private int priority;

    public List<String> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<String> testCases) {
        this.testCases = testCases;
    }

    public List<String> getIgnoredTests() {
        return ignoredTests;
    }

    public void setIgnoredTests(List<String> ignoredTests) {
        this.ignoredTests = ignoredTests;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int compareTo(Object o) {
        return priority != ((TestBean)o).getPriority() ? priority - ((TestBean)o).getPriority() : 1;
    }
}
