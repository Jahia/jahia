/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.jcr.node;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.junit.Test;
import org.mockito.Mockito;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Christophe Laprun
 */
public class NodeComparatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void comparatorWithNullPropsShouldFail() {
        new JCRSortTag.NodeComparator(null);
    }

    @Test
    public void comparingSameShouldReturnZero() throws RepositoryException {
        final JCRSortTag.NodeComparator comparator = new JCRSortTag.NodeComparator(new String[]{"prop1", "asc"});
        final JCRNodeWrapper nodeWrapper = mockNode("value1", "value2");
        assertEquals(0, comparator.compare(nodeWrapper, nodeWrapper));

        assertEquals(0, comparator.compare(nodeWrapper, mockNode("value1")));
    }

    @Test
    public void comparingShouldBeTransitive() throws RepositoryException {
        final JCRSortTag.NodeComparator comparator = new JCRSortTag.NodeComparator(new String[]{"prop1", "asc"});
        final JCRNodeWrapper first = mockNode("value1");
        final JCRNodeWrapper second = mockNode("value2");
        final JCRNodeWrapper third = mockNode("value3");

        assertTrue(comparator.compare(second, first) > 0);
        assertTrue(comparator.compare(third, second) > 0);
        assertTrue(comparator.compare(third, first) > 0);
    }

    @Test
    public void comparingWithCaseSensitivityShouldWork() throws RepositoryException {
        JCRSortTag.NodeComparator comparator = new JCRSortTag.NodeComparator(new String[]{"false", "prop1", "asc"});
        final JCRNodeWrapper lower = mockNode("value1");
        final JCRNodeWrapper upper = mockNode("VALUE1");

        assertEquals(0, comparator.compare(lower, upper));
        assertEquals(0, comparator.compare(upper, lower));

        comparator = new JCRSortTag.NodeComparator(new String[]{"true", "prop1", "asc"});
        assertTrue(comparator.compare(lower, upper) < 0);
    }

    @Test
    public void comparingOnMultiplePropsShouldWork() throws RepositoryException {
        final JCRSortTag.NodeComparator comparator = new JCRSortTag.NodeComparator(new String[]{"prop1", "asc", "prop3", "desc"});
        final JCRNodeWrapper bigger = mockNode("value1", "foo2", "bar1");
        final JCRNodeWrapper smaller = mockNode("value1", "foo2", "bar2");
        final JCRNodeWrapper biggest = mockNode("value1", "foo2", "bar0");

        assertTrue(comparator.compare(bigger, smaller) > 0);
        assertTrue(comparator.compare(smaller, bigger) < 0);
        assertTrue(comparator.compare(biggest, bigger) > 0);
        assertTrue(comparator.compare(biggest, smaller) > 0);
        assertEquals(comparator.compare(bigger, smaller), -comparator.compare(smaller, bigger));
    }

    @Test
    public void comparingSameObjectsInDifferentOrderShouldReturnOppositeResults() throws RepositoryException {
        final JCRSortTag.NodeComparator comparator = new JCRSortTag.NodeComparator(new String[]{"prop1", "asc", "prop3", "desc"});
        final JCRNodeWrapper smaller = mockNode("value1", "foo2", "bar2");
        final JCRNodeWrapper bigger = mockNode("value2", "foo2", "bar1");

        assertTrue(comparator.compare(smaller, bigger) < 0);
        assertTrue(comparator.compare(bigger, smaller) > 0);
        assertEquals(comparator.compare(smaller, bigger), -comparator.compare(bigger, smaller));
    }

    private JCRNodeWrapper mockNode(String... propValues) throws RepositoryException {
        final JCRNodeWrapper nodeWrapper = mock(JCRNodeWrapper.class);
        int i = 1;
        for (String value : propValues) {
            final String propName = "prop" + i++;
            when(nodeWrapper.hasProperty(propName)).thenReturn(true);
            final JCRPropertyWrapper prop = mockProperty(value);
            when(nodeWrapper.getProperty(propName)).thenReturn(prop);
        }

        when(nodeWrapper.getLanguage()).thenReturn("en");

        return nodeWrapper;
    }

    private JCRPropertyWrapper mockProperty(String value) throws RepositoryException {
        final JCRPropertyWrapper property = Mockito.mock(JCRPropertyWrapper.class);
        when(property.getType()).thenReturn(PropertyType.STRING);
        when(property.getString()).thenReturn(value);
        return property;
    }
}
