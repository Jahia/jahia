/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
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
        String query = "j:tagList###tag1###3056820\\:FACET\\:tags:4c1b0348\\-89d0\\-461e\\-b31d\\-d725b8e6ea18|||j:tagList###tag2###3056820\\:FACET\\:tags:cdc62535\\-bcac\\-44d7\\-b4da\\-be8c865d7a58";
        KeyValue facetValue1 = new DefaultKeyValue("tag1", "3056820\\:FACET\\:tags:4c1b0348\\-89d0\\-461e\\-b31d\\-d725b8e6ea18");
        KeyValue facetValue2 = new DefaultKeyValue("tag2", "3056820\\:FACET\\:tags:cdc62535\\-bcac\\-44d7\\-b4da\\-be8c865d7a58");


        assertEquals("j:tagList###tag2###3056820\\:FACET\\:tags:cdc62535\\-bcac\\-44d7\\-b4da\\-be8c865d7a58", Functions.getDeleteFacetUrl(facetValue1, query));
        assertEquals("j:tagList###tag1###3056820\\:FACET\\:tags:4c1b0348\\-89d0\\-461e\\-b31d\\-d725b8e6ea18", Functions.getDeleteFacetUrl(facetValue2, query));
    }

} 
