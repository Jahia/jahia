/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.query;


import junit.framework.Assert;
import org.apache.jackrabbit.rmi.iterator.ArrayIterator;
import org.jahia.services.content.MultipleIterator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test of {@link org.jahia.services.content.MultipleIterator}
 */
public class MultipleIteratorTest {

    @Test
    public void testMultipleIterator() {
        List<ArrayIterator> arrayIterators = new ArrayList<ArrayIterator>();
        arrayIterators.add(new ArrayIterator(new String[] {"foo", "bar"}));
        arrayIterators.add(new ArrayIterator(new String[] {}));
        arrayIterators.add(new ArrayIterator(new String[] {"spam", "ham", "eggs"}));
        MultipleIterator<ArrayIterator> multipleArrayIterator = new MultipleIterator<ArrayIterator>(arrayIterators, -1);
        multipleArrayIterator.skip(3);
        Assert.assertEquals(true, multipleArrayIterator.hasNext());
        Assert.assertEquals("ham", multipleArrayIterator.next());
        Assert.assertEquals("eggs", multipleArrayIterator.next());
        Assert.assertEquals(false, multipleArrayIterator.hasNext());
    }

    @Test
    public void testMultipleIteratorWithLimit1() {
        List<ArrayIterator> arrayIterators = new ArrayList<ArrayIterator>();
        arrayIterators.add(new ArrayIterator(new String[] {"foo", "bar"}));
        arrayIterators.add(new ArrayIterator(new String[] {}));
        arrayIterators.add(new ArrayIterator(new String[] {"spam", "ham", "eggs"}));
        MultipleIterator<ArrayIterator> multipleArrayIterator = new MultipleIterator<ArrayIterator>(arrayIterators, 1);
        Assert.assertEquals(true, multipleArrayIterator.hasNext());
        Assert.assertEquals("foo", multipleArrayIterator.next());
        Assert.assertEquals(false, multipleArrayIterator.hasNext());
        Assert.assertEquals(1, multipleArrayIterator.getSize());
    }

    @Test
    public void testMultipleIteratorWithLimit2() {
        List<ArrayIterator> arrayIterators = new ArrayList<ArrayIterator>();
        arrayIterators.add(new ArrayIterator(new String[] {"foo", "bar"}));
        arrayIterators.add(new ArrayIterator(new String[] {}));
        arrayIterators.add(new ArrayIterator(new String[] {"spam", "ham", "eggs"}));
        MultipleIterator<ArrayIterator> multipleArrayIterator = new MultipleIterator<ArrayIterator>(arrayIterators, 2);
        Assert.assertEquals(true, multipleArrayIterator.hasNext());
        Assert.assertEquals("foo", multipleArrayIterator.next());
        Assert.assertEquals("bar", multipleArrayIterator.next());
        Assert.assertEquals(false, multipleArrayIterator.hasNext());
        Assert.assertEquals(2, multipleArrayIterator.getSize());
    }

    @Test
    public void testMultipleIteratorWithLimit3() {
        List<ArrayIterator> arrayIterators = new ArrayList<ArrayIterator>();
        arrayIterators.add(new ArrayIterator(new String[] {"foo", "bar"}));
        arrayIterators.add(new ArrayIterator(new String[] {}));
        arrayIterators.add(new ArrayIterator(new String[] {"spam", "ham", "eggs"}));
        MultipleIterator<ArrayIterator> multipleArrayIterator = new MultipleIterator<ArrayIterator>(arrayIterators, 3);
        Assert.assertEquals(true, multipleArrayIterator.hasNext());
        Assert.assertEquals("foo", multipleArrayIterator.next());
        Assert.assertEquals("bar", multipleArrayIterator.next());
        Assert.assertEquals("spam", multipleArrayIterator.next());
        Assert.assertEquals(false, multipleArrayIterator.hasNext());
        Assert.assertEquals(3, multipleArrayIterator.getSize());
    }
}
