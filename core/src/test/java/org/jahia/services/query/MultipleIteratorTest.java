/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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


import org.junit.Assert;
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
