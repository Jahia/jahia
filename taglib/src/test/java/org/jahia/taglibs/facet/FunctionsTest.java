package org.jahia.taglibs.facet;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

/**
 * Functions Tester.
 *
 * @author Christophe Laprun
 * @since <pre>Oct 17, 2013</pre>
 */
@RunWith(JUnit4.class)
public class FunctionsTest {

    /**
     * Method: getDeleteFacetUrl(Object facetFilterObj, KeyValue facetValue, String queryString)
     */
    @Test
    public void testGetDeleteFacetUrl() throws Exception {
        String query = "j:tags###4c1b0348-89d0-461e-b31d-d725b8e6ea18###3056820\\:FACET\\:tags:4c1b0348\\-89d0\\-461e\\-b31d\\-d725b8e6ea18|||j:tags###cdc62535-bcac-44d7-b4da-be8c865d7a58###3056820\\:FACET\\:tags:cdc62535\\-bcac\\-44d7\\-b4da\\-be8c865d7a58";
        KeyValue facetValue1 = new DefaultKeyValue("4c1b0348-89d0-461e-b31d-d725b8e6ea18", "3056820\\:FACET\\:tags:4c1b0348\\-89d0\\-461e\\-b31d\\-d725b8e6ea18");
        KeyValue facetValue2 = new DefaultKeyValue("cdc62535-bcac-44d7-b4da-be8c865d7a58", "3056820\\:FACET\\:tags:cdc62535\\-bcac\\-44d7\\-b4da\\-be8c865d7a58");


        assertEquals("j:tags###cdc62535-bcac-44d7-b4da-be8c865d7a58###3056820\\:FACET\\:tags:cdc62535\\-bcac\\-44d7\\-b4da\\-be8c865d7a58", Functions.getDeleteFacetUrl(facetValue1, query));
        assertEquals("j:tags###4c1b0348-89d0-461e-b31d-d725b8e6ea18###3056820\\:FACET\\:tags:4c1b0348\\-89d0\\-461e\\-b31d\\-d725b8e6ea18", Functions.getDeleteFacetUrl(facetValue2, query));
    }

} 
