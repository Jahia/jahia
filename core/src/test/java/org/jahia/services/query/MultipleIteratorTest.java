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
