package org.jahia.taglibs.facet;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Functions Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Oct 17, 2013</pre>
 */
@RunWith(JUnit4.class)
public class FunctionsTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getAppliedFacetFilters(String filterString)
     */
    @Test
    public void testGetAppliedFacetFilters() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: isFacetApplied(String facetName, Map<String, List<KeyValue>> appliedFacets, PropertyDefinition propDef)
     */
    @Test
    public void testIsFacetApplied() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: isFacetValueApplied(Object facetValueObj, Map<String, List<KeyValue>> appliedFacets)
     */
    @Test
    public void testIsFacetValueApplied() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getFacetDrillDownUrl(Object facetValueObj, String queryString)
     */
    @Test
    public void testGetFacetDrillDownUrl() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getDeleteFacetUrl(Object facetFilterObj, KeyValue facetValue, String queryString)
     */
    @Test
    public void testGetDeleteFacetUrl() throws Exception {
        /*String query = "j:tags###4c1b0348-89d0-461e-b31d-d725b8e6ea18###3056820\\:FACET\\:tags:4c1b0348\\-89d0\\-461e\\-b31d\\-d725b8e6ea18|||j:tags###cdc62535-bcac-44d7-b4da-be8c865d7a58###3056820\\:FACET\\:tags:cdc62535\\-bcac\\-44d7\\-b4da\\-be8c865d7a58";
        KeyValue facetValue1 = new DefaultKeyValue("4c1b0348-89d0-461e-b31d-d725b8e6ea18", "3056820\\:FACET\\:tags:4c1b0348\\-89d0\\-461e\\-b31d\\-d725b8e6ea18");
        KeyValue facetValue2 = new DefaultKeyValue("cdc62535-bcac-44d7-b4da-be8c865d7a58", "3056820\\:FACET\\:tags:cdc62535\\-bcac\\-44d7\\-b4da\\-be8c865d7a58");
        final ArrayList<KeyValue> values = new ArrayList<KeyValue>();
        values.add(facetValue1);
        values.add(facetValue2);
        Map.Entry<String, List<KeyValue>> appliedFilters = new StringListEntry("j:tags", values);

        assertEquals("j:tags###cdc62535-bcac-44d7-b4da-be8c865d7a58###3056820\\:FACET\\:tags:cdc62535\\-bcac\\-44d7\\-b4da\\-be8c865d7a58", Functions.getDeleteFacetUrl(appliedFilters, facetValue1, query));
        assertEquals("j:tags###4c1b0348-89d0-461e-b31d-d725b8e6ea18###3056820\\:FACET\\:tags:4c1b0348\\-89d0\\-461e\\-b31d\\-d725b8e6ea18", Functions.getDeleteFacetUrl(appliedFilters, facetValue2, query));*/
    }

    /**
     * Method: isUnappliedFacetExisting(QueryResultWrapper result, Map<String, List<KeyValue>> appliedFacets)
     */
    @Test
    public void testIsUnappliedFacetExisting() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: isUnappliedFacetValueExisting(FacetField facetField, Map<String, List<KeyValue>> appliedFacets)
     */
    @Test
    public void testIsUnappliedFacetValueExisting() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getDrillDownPrefix(String hierarchicalFacet)
     */
    @Test
    public void testGetDrillDownPrefix() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getPropertyDefinitions(JCRNodeWrapper facet)
     */
    @Test
    public void testGetPropertyDefinitions() throws Exception {
//TODO: Test goes here... 
    }

    /**
     * Method: getIndexPrefixedPath(final String facetPath)
     */
    @Test
    public void testGetIndexPrefixedPath() throws Exception {
//TODO: Test goes here... 
    }

    private static class StringListEntry implements Map.Entry<String, List<KeyValue>> {
        private String key;
        private List<KeyValue> value;

        public StringListEntry(String key, List<KeyValue> value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public List<KeyValue> getValue() {
            return value;
        }

        public List<KeyValue> setValue(List<KeyValue> value) {
            throw new UnsupportedOperationException();
        }
    }


} 
