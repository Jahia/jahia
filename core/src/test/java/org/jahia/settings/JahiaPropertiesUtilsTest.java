package org.jahia.settings;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

public class JahiaPropertiesUtilsTest {

    private File testPropertiesFile;

    @Before
    public void setUp() throws IOException {
        testPropertiesFile = File.createTempFile("jahia-test", ".properties");
    }

    @After
    public void tearDown() {
        if (testPropertiesFile != null && testPropertiesFile.exists()) {
            testPropertiesFile.delete();
        }
    }

    @Test
    public void testAddCommentBeforeActiveProperty() throws Exception {
        // Setup
        List<String> lines = List.of(
                "some.property = value1",
                "test.property = testValue",
                "another.property = value2"
        );
        FileUtils.writeLines(testPropertiesFile, lines);

        // Execute
        JahiaPropertiesUtils.addCommentBeforeProperty("test.property", "This is a deprecation comment", testPropertiesFile);

        // Verify
        List<String> result = FileUtils.readLines(testPropertiesFile, StandardCharsets.UTF_8);
        assertEquals(4, result.size());
        assertEquals("some.property = value1", result.get(0));
        assertEquals("# This is a deprecation comment", result.get(1));
        assertEquals("test.property = testValue", result.get(2));
        assertEquals("another.property = value2", result.get(3));
    }

    @Test
    public void testAddCommentBeforeCommentedProperty() throws Exception {
        // Setup
        List<String> lines = List.of(
                "some.property = value1",
                "# test.property = testValue",
                "another.property = value2"
        );
        FileUtils.writeLines(testPropertiesFile, lines);

        // Execute
        JahiaPropertiesUtils.addCommentBeforeProperty("test.property", "Deprecation notice", testPropertiesFile);

        // Verify
        List<String> result = FileUtils.readLines(testPropertiesFile, StandardCharsets.UTF_8);
        assertEquals(4, result.size());
        assertEquals("some.property = value1", result.get(0));
        assertEquals("# Deprecation notice", result.get(1));
        assertEquals("# test.property = testValue", result.get(2));
    }

    @Test
    public void testDoNotAddDuplicateComment() throws Exception {
        // Setup
        List<String> lines = List.of(
                "some.property = value1",
                "# This is a deprecation comment",
                "test.property = testValue",
                "another.property = value2"
        );
        FileUtils.writeLines(testPropertiesFile, lines);

        // Execute
        JahiaPropertiesUtils.addCommentBeforeProperty("test.property", "This is a deprecation comment", testPropertiesFile);

        // Verify - no change
        List<String> result = FileUtils.readLines(testPropertiesFile, StandardCharsets.UTF_8);
        assertEquals(4, result.size());
        assertEquals("# This is a deprecation comment", result.get(1));
        assertEquals("test.property = testValue", result.get(2));
    }

    @Test
    public void testAddCommentWithHashPrefix() throws Exception {
        // Setup
        List<String> lines = List.of(
                "test.property = testValue"
        );
        FileUtils.writeLines(testPropertiesFile, lines);

        // Execute - comment already has # prefix
        JahiaPropertiesUtils.addCommentBeforeProperty("test.property", "# Already prefixed comment", testPropertiesFile);

        // Verify
        List<String> result = FileUtils.readLines(testPropertiesFile, StandardCharsets.UTF_8);
        assertEquals(2, result.size());
        assertEquals("# Already prefixed comment", result.get(0));
        assertEquals("test.property = testValue", result.get(1));
    }

    @Test
    public void testAddCommentToPropertyWithSpacing() throws Exception {
        // Setup
        List<String> lines = List.of(
                "  test.property  =  testValue  "
        );
        FileUtils.writeLines(testPropertiesFile, lines);

        // Execute
        JahiaPropertiesUtils.addCommentBeforeProperty("test.property", "Comment", testPropertiesFile);

        // Verify
        List<String> result = FileUtils.readLines(testPropertiesFile, StandardCharsets.UTF_8);
        assertEquals(2, result.size());
        assertEquals("# Comment", result.get(0));
        assertTrue(result.get(1).contains("test.property"));
    }

    @Test
    public void testPropertyNotFound() throws Exception {
        // Setup
        List<String> lines = List.of(
                "some.property = value1",
                "another.property = value2"
        );
        FileUtils.writeLines(testPropertiesFile, lines);

        // Execute
        JahiaPropertiesUtils.addCommentBeforeProperty("non.existent.property", "Comment", testPropertiesFile);

        // Verify - no change
        List<String> result = FileUtils.readLines(testPropertiesFile, StandardCharsets.UTF_8);
        assertEquals(2, result.size());
        assertFalse(result.stream().anyMatch(line -> line.contains("Comment")));
    }

    @Test
    public void testDoNotAddCommentToPropertyInComment() throws Exception {
        // Setup - property key appears in a comment but not as an actual property
        List<String> lines = List.of(
                "some.property = value1",
                "# This is a comment mentioning test.property but it's not the actual property",
                "another.property = value2"
        );
        FileUtils.writeLines(testPropertiesFile, lines);

        // Execute
        JahiaPropertiesUtils.addCommentBeforeProperty("test.property", "Deprecation notice", testPropertiesFile);

        // Verify - no change because test.property only exists in a comment, not as an actual property
        List<String> result = FileUtils.readLines(testPropertiesFile, StandardCharsets.UTF_8);
        assertEquals(3, result.size());
        assertEquals("some.property = value1", result.get(0));
        assertEquals("# This is a comment mentioning test.property but it's not the actual property", result.get(1));
        assertEquals("another.property = value2", result.get(2));
        assertFalse(result.stream().anyMatch(line -> line.contains("Deprecation notice")));
    }

    // ========== Tests for getPropertyLine(String key, String content) ==========

    @Test
    public void testGetPropertyLineFromString_SimpleProperty() {
        String content = "some.property=value1\ntest.property=testValue\nanother.property=value2";

        String result = JahiaPropertiesUtils.getPropertyLine("test.property", content);

        assertNotNull(result);
        assertEquals("test.property=testValue", result);
    }

    @Test
    public void testGetPropertyLineFromString_PropertyWithSpaces() {
        String content = "some.property=value1\n  test.property  =  testValue  \nanother.property=value2";

        String result = JahiaPropertiesUtils.getPropertyLine("test.property", content);

        assertNotNull(result);
        assertTrue(result.contains("test.property"));
        assertTrue(result.contains("testValue"));
    }

    @Test
    public void testGetPropertyLineFromString_PropertyWithTabs() {
        String content = "some.property=value1\n\ttest.property\t:\ttestValue\t\nanother.property=value2";

        String result = JahiaPropertiesUtils.getPropertyLine("test.property", content);

        assertNotNull(result);
        assertTrue(result.contains("test.property"));
        assertTrue(result.contains("testValue"));
    }

    @Test
    public void testGetPropertyLineFromString_PropertyWithColon() {
        String content = "some.property=value1\ntest.property:testValue\nanother.property=value2";

        String result = JahiaPropertiesUtils.getPropertyLine("test.property", content);

        assertNotNull(result);
        assertEquals("test.property:testValue", result);
    }

    @Test
    public void testGetPropertyLineFromString_PropertyNotFound() {
        String content = "some.property=value1\nanother.property=value2";

        String result = JahiaPropertiesUtils.getPropertyLine("non.existent.property", content);

        assertNull(result);
    }

    @Test
    public void testGetPropertyLineFromString_CommentedPropertyNotMatched() {
        String content = "some.property=value1\n# test.property=testValue\nanother.property=value2";

        String result = JahiaPropertiesUtils.getPropertyLine("test.property", content);

        assertNull(result);
    }

    @Test
    public void testGetPropertyLineFromString_EmptyContent() {
        String content = "";

        String result = JahiaPropertiesUtils.getPropertyLine("test.property", content);

        assertNull(result);
    }

    @Test
    public void testGetPropertyLineFromString_MultipleOccurrences_ReturnsFirst() {
        String content = "test.property=value1\nsome.property=value2\ntest.property=value3";

        String result = JahiaPropertiesUtils.getPropertyLine("test.property", content);

        assertNotNull(result);
        assertEquals("test.property=value1", result);
    }

    @Test
    public void testGetPropertyLineFromString_PropertyKeyAsSubstring() {
        String content = "test.property=value1\ntest.property.extended=value2";

        String result = JahiaPropertiesUtils.getPropertyLine("test.property", content);

        assertNotNull(result);
        assertEquals("test.property=value1", result);
    }

    @Test
    public void testGetPropertyLineFromString_SpecialCharactersInKey() {
        String content = "some.property=value1\ntest.property$special=testValue\nanother.property=value2";

        String result = JahiaPropertiesUtils.getPropertyLine("test.property$special", content);

        assertNotNull(result);
        assertEquals("test.property$special=testValue", result);
    }

    // ========== Tests for getPropertyLine(String key, List<String> lines) ==========

    @Test
    public void testGetPropertyLineFromList_SimpleProperty() {
        List<String> lines = List.of(
                "some.property=value1",
                "test.property=testValue",
                "another.property=value2"
        );

        String result = JahiaPropertiesUtils.getPropertyLine("test.property", lines);

        assertNotNull(result);
        assertEquals("test.property=testValue", result);
    }

    @Test
    public void testGetPropertyLineFromList_PropertyWithSpaces() {
        List<String> lines = List.of(
                "some.property=value1",
                "  test.property  =  testValue  ",
                "another.property=value2"
        );

        String result = JahiaPropertiesUtils.getPropertyLine("test.property", lines);

        assertNotNull(result);
        assertTrue(result.contains("test.property"));
        assertTrue(result.contains("testValue"));
    }

    @Test
    public void testGetPropertyLineFromList_PropertyNotFound() {
        List<String> lines = List.of(
                "some.property=value1",
                "another.property=value2"
        );

        String result = JahiaPropertiesUtils.getPropertyLine("non.existent.property", lines);

        assertNull(result);
    }

    @Test
    public void testGetPropertyLineFromList_EmptyList() {
        List<String> lines = List.of();

        String result = JahiaPropertiesUtils.getPropertyLine("test.property", lines);

        assertNull(result);
    }

    // ========== Tests for getCommentedPropertyLine(String key, String content) ==========

    @Test
    public void testGetCommentedPropertyLineFromString_SimpleCommentedProperty() {
        String content = "some.property=value1\n# test.property=testValue\nanother.property=value2";

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", content);

        assertNotNull(result);
        assertEquals("# test.property=testValue", result);
    }

    @Test
    public void testGetCommentedPropertyLineFromString_ExclamationComment() {
        String content = "some.property=value1\n! test.property=testValue\nanother.property=value2";

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", content);

        assertNotNull(result);
        assertEquals("! test.property=testValue", result);
    }

    @Test
    public void testGetCommentedPropertyLineFromString_MultipleHashSigns() {
        String content = "some.property=value1\n## test.property=testValue\nanother.property=value2";

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", content);

        assertNotNull(result);
        assertEquals("## test.property=testValue", result);
    }

    @Test
    public void testGetCommentedPropertyLineFromString_CommentedPropertyWithSpaces() {
        String content = "some.property=value1\n  #  test.property  =  testValue  \nanother.property=value2";

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", content);

        assertNotNull(result);
        assertTrue(result.contains("#"));
        assertTrue(result.contains("test.property"));
        assertTrue(result.contains("testValue"));
    }

    @Test
    public void testGetCommentedPropertyLineFromString_CommentedPropertyWithColon() {
        String content = "some.property=value1\n# test.property:testValue\nanother.property=value2";

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", content);

        assertNotNull(result);
        assertEquals("# test.property:testValue", result);
    }

    @Test
    public void testGetCommentedPropertyLineFromString_ActivePropertyNotMatched() {
        String content = "some.property=value1\ntest.property=testValue\nanother.property=value2";

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", content);

        assertNull(result);
    }

    @Test
    public void testGetCommentedPropertyLineFromString_PropertyNotFound() {
        String content = "some.property=value1\n# another.property=value2";

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("non.existent.property", content);

        assertNull(result);
    }

    @Test
    public void testGetCommentedPropertyLineFromString_EmptyContent() {
        String content = "";

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", content);

        assertNull(result);
    }

    @Test
    public void testGetCommentedPropertyLineFromString_CommentTextNotMatched() {
        String content = "some.property=value1\n# This is a comment mentioning test.property but not the actual property\nanother.property=value2";

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", content);

        assertNull(result);
    }

    // ========== Tests for getCommentedPropertyLine(String key, List<String> lines) ==========

    @Test
    public void testGetCommentedPropertyLineFromList_SimpleCommentedProperty() {
        List<String> lines = List.of(
                "some.property=value1",
                "# test.property=testValue",
                "another.property=value2"
        );

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", lines);

        assertNotNull(result);
        assertEquals("# test.property=testValue", result);
    }

    @Test
    public void testGetCommentedPropertyLineFromList_ExclamationComment() {
        List<String> lines = List.of(
                "some.property=value1",
                "! test.property:testValue",
                "another.property=value2"
        );

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", lines);

        assertNotNull(result);
        assertEquals("! test.property:testValue", result);
    }

    @Test
    public void testGetCommentedPropertyLineFromList_PropertyNotFound() {
        List<String> lines = List.of(
                "some.property=value1",
                "# another.property=value2"
        );

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("non.existent.property", lines);

        assertNull(result);
    }

    @Test
    public void testGetCommentedPropertyLineFromList_EmptyList() {
        List<String> lines = List.of();

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", lines);

        assertNull(result);
    }

    @Test
    public void testGetCommentedPropertyLineFromList_ActivePropertyNotMatched() {
        List<String> lines = List.of(
                "some.property=value1",
                "test.property=testValue",
                "another.property=value2"
        );

        String result = JahiaPropertiesUtils.getCommentedPropertyLine("test.property", lines);

        assertNull(result);
    }

}
