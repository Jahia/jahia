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
