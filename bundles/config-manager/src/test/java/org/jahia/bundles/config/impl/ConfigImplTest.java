package org.jahia.bundles.config.impl;

import org.jahia.bundles.config.Format;
import org.junit.Test;
import org.osgi.service.cm.Configuration;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ConfigImpl}.
 */
public class ConfigImplTest {

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Configuration mockConfiguration(Dictionary<String, Object> dict) {
        Configuration conf = mock(Configuration.class);
        when(conf.getProperties()).thenReturn(dict);
        return conf;
    }

    // -------------------------------------------------------------------------
    // Scalar property
    // -------------------------------------------------------------------------

    /**
     * Verifies that a regular scalar String property is stored as-is.
     */
    @Test
    public void GIVEN_configWithStringProperty_WHEN_readingRawProperties_THEN_valueIsUnchanged() {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put("myProp", "myValue");
        ConfigImpl config = new ConfigImpl(mockConfiguration(dict), null, Format.CFG);

        Map<String, String> props = config.getRawProperties();

        assertEquals(1, props.size());
        assertEquals("myValue", props.get("myProp"));
    }

    // -------------------------------------------------------------------------
    // Array expansion
    // -------------------------------------------------------------------------

    /**
     * Verifies that a {@code String[]} multi-value property is expanded into indexed entries.
     */
    @Test
    public void GIVEN_configWithArrayProperty_WHEN_readingRawProperties_THEN_valuesAreExpandedIntoIndexedEntries() {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put("multiProp", new String[] { "alpha", "beta", "gamma" });
        ConfigImpl config = new ConfigImpl(mockConfiguration(dict), null, Format.CFG);

        Map<String, String> props = config.getRawProperties();

        assertEquals(3, props.size());
        assertEquals("alpha", props.get("multiProp[0]"));
        assertEquals("beta", props.get("multiProp[1]"));
        assertEquals("gamma", props.get("multiProp[2]"));
    }

    /**
     * Verifies that a single-element array produces a single indexed entry, not a plain key.
     */
    @Test
    public void GIVEN_configWithSingleElementArray_WHEN_readingRawProperties_THEN_singleIndexedEntryIsCreated() {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put("prop", new String[] { "only" });
        ConfigImpl config = new ConfigImpl(mockConfiguration(dict), null, Format.CFG);

        Map<String, String> props = config.getRawProperties();

        assertEquals(1, props.size());
        assertEquals("only", props.get("prop[0]"));
        assertNull(props.get("prop"));
    }

    /**
     * Verifies that an empty array produces no entries for that key.
     */
    @Test
    public void GIVEN_configWithEmptyArray_WHEN_readingRawProperties_THEN_noEntryIsCreated() {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put("prop", new String[0]);
        ConfigImpl config = new ConfigImpl(mockConfiguration(dict), null, Format.CFG);

        Map<String, String> props = config.getRawProperties();

        assertEquals(0, props.size());
    }

    /**
     * Verifies that null elements inside an array are skipped while preserving the index of subsequent elements.
     */
    @Test
    public void GIVEN_arrayWithNullElements_WHEN_readingRawProperties_THEN_nullElementsAreSkipped() {
        Dictionary<String, Object> dict = new Hashtable<>();
        // Hashtable doesn't accept null values, so wrap in Object[] to carry nulls
        dict.put("prop", new Object[] { "a", null, "b" });
        ConfigImpl config = new ConfigImpl(mockConfiguration(dict), null, Format.CFG);

        Map<String, String> props = config.getRawProperties();

        // prop[1] is absent because its element was null
        assertEquals(2, props.size());
        assertEquals("a", props.get("prop[0]"));
        assertNull(props.get("prop[1]"));
        assertEquals("b", props.get("prop[2]"));
    }

    /**
     * Verifies that a {@code Collection} value is expanded into indexed entries the same way as an array.
     */
    @Test
    public void GIVEN_configWithCollectionProperty_WHEN_readingRawProperties_THEN_valuesAreExpandedIntoIndexedEntries() {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put("prop", List.of("x", "y", "z"));
        ConfigImpl config = new ConfigImpl(mockConfiguration(dict), null, Format.CFG);

        Map<String, String> props = config.getRawProperties();

        assertEquals(3, props.size());
        assertEquals("x", props.get("prop[0]"));
        assertEquals("y", props.get("prop[1]"));
        assertEquals("z", props.get("prop[2]"));
    }

    // -------------------------------------------------------------------------
    // OSGi system key filtering
    // -------------------------------------------------------------------------

    /**
     * Verifies that keys starting with {@code felix.} are excluded from the map.
     */
    @Test
    public void GIVEN_configWithFelixKey_WHEN_readingRawProperties_THEN_felixKeyIsExcluded() {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put("felix.fileinstall.filename", "/some/path/foo.cfg");
        dict.put("normalProp", "value");
        ConfigImpl config = new ConfigImpl(mockConfiguration(dict), null, Format.CFG);

        Map<String, String> props = config.getRawProperties();

        assertEquals(1, props.size());
        assertFalse(props.containsKey("felix.fileinstall.filename"));
        assertEquals("value", props.get("normalProp"));
    }

    /**
     * Verifies that keys starting with {@code service.} are excluded from the map.
     */
    @Test
    public void GIVEN_configWithServiceKey_WHEN_readingRawProperties_THEN_serviceKeyIsExcluded() {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put("service.pid", "some.pid");
        dict.put("normalProp", "value");
        ConfigImpl config = new ConfigImpl(mockConfiguration(dict), null, Format.CFG);

        Map<String, String> props = config.getRawProperties();

        assertEquals(1, props.size());
        assertFalse(props.containsKey("service.pid"));
        assertEquals("value", props.get("normalProp"));
    }

    // -------------------------------------------------------------------------
    // Mutation and round-trip
    // -------------------------------------------------------------------------

    /**
     * Verifies that adding an unrelated property does not corrupt existing multi-value entries.
     */
    @Test
    public void GIVEN_configWithArrayProperty_WHEN_addingAnotherProperty_THEN_multiValueEntriesArePreserved() {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put("multiProp", new String[] { "alpha", "beta", "gamma" });
        ConfigImpl config = new ConfigImpl(mockConfiguration(dict), null, Format.CFG);

        config.getValues().setProperty("anotherProp", "true");

        Map<String, String> props = config.getRawProperties();

        // multiProp[0,1,2] + anotherProp
        assertEquals(4, props.size());
        assertEquals("true", props.get("anotherProp"));
        assertEquals("alpha", props.get("multiProp[0]"));
        assertEquals("beta", props.get("multiProp[1]"));
        assertEquals("gamma", props.get("multiProp[2]"));
    }

    /**
     * Verifies that removing a multi-value property removes all its indexed entries.
     */
    @Test
    public void GIVEN_configWithArrayProperty_WHEN_removingMultiValueProperty_THEN_allIndexedEntriesAreRemoved() {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put("multiProp", new String[] { "alpha", "beta", "gamma" });
        ConfigImpl config = new ConfigImpl(mockConfiguration(dict), null, Format.CFG);

        config.getValues().remove("multiProp");

        Map<String, String> props = config.getRawProperties();

        assertEquals(0, props.size());
    }

    /**
     * Verifies that serializing a config with multi-value properties to CFG format and reading it back
     * produces the same indexed entries.
     */
    @Test
    public void GIVEN_configWithArrayProperty_WHEN_serializingAndDeserializingCfgContent_THEN_indexedEntriesArePreserved()
            throws IOException {
        Dictionary<String, Object> dict = new Hashtable<>();
        dict.put("multiProp", new String[] { "alpha", "beta", "gamma" });
        ConfigImpl config = new ConfigImpl(mockConfiguration(dict), null, Format.CFG);

        String content = config.getContent();
        config.setContent(content);

        Map<String, String> props = config.getRawProperties();

        assertEquals(3, props.size());
        assertEquals("alpha", props.get("multiProp[0]"));
        assertEquals("beta", props.get("multiProp[1]"));
        assertEquals("gamma", props.get("multiProp[2]"));
    }
}

