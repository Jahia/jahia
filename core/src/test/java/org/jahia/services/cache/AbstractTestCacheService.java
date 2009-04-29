/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.cache;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.jahia.exceptions.JahiaInitializationException;

/**
 * User: Serge Huber
 * Date: Jul 15, 2005
 * Time: 12:04:44 PM
 * Copyright (C) Jahia Inc.
 */
public abstract class AbstractTestCacheService extends TestCase {
    private static final int FILL_COUNT_ENTRIES = 1000000;
	protected CacheService cacheService = null;

    public void testGroups() throws JahiaInitializationException {
        Cache testCache = cacheService.createCacheInstance("testCache");
        testCache.put("1", "1");
        testCache.put("2", "2");
        Set groups = new HashSet();
        groups.add("1");
        groups.add("3");
        GroupCacheKey groupKey1 = new GroupCacheKey(groups);
        System.out.println("groupKey1=" + groupKey1);
        testCache.put(groupKey1, "3");
        Object value2 = testCache.get("2");
        assertNotNull(value2);
        Object value3 = testCache.get(groupKey1);
        assertNotNull(value3);
        testCache.flushGroup("1");
        Object value1 = testCache.get("1");
        assertNotNull(value1);
        value3 = testCache.get(groupKey1);
        assertNull(value3);
        testCache.put(groupKey1, "3");
        testCache.flushGroup("3");
        value3 = testCache.get(groupKey1);
        assertNull(value3);
    }

    public void testRegionSeperation() throws JahiaInitializationException {
        Cache testCache1 = cacheService.createCacheInstance("testCache1");
        Cache testCache2 = cacheService.createCacheInstance("testCache2");

        testCache1.put("1", "1");
        testCache1.flush();
        testCache2.put("1", "1");
        assertNotNull(testCache2.get("1"));
    }

    public void testCacheReadSpeed() throws JahiaInitializationException {
        Cache testCache = cacheService.createCacheInstance("testCache");
        for (int i=0; i < 100; i++) {
            testCache.put(Integer.toString(i), Integer.toString(i));
        }
        long startTime = System.currentTimeMillis();
        for (int i=0; i < 50000; i++) {
            Object value = testCache.get("50");
        }
        long stopTime = System.currentTimeMillis();
        System.out.println("Cache read speed test completed in " + (stopTime - startTime) + "ms");
    }

    public void testGroupCacheWriteSpeed() throws JahiaInitializationException {
        Cache testCache = cacheService.createCacheInstance("testCache");
        testCache.flush();
        long startTime = System.currentTimeMillis();
        for (int i=0; i < 50000; i++) {
            Set groups = new HashSet();
            groups.add("group1");
            groups.add("group2");
            groups.add(Integer.toString(i));
            groups.add("group3");
            GroupCacheKey curKey = new GroupCacheKey(groups);
            testCache.put(curKey, Integer.toString(i));
            testCache.get(curKey);
        }
        testCache.flushGroup("group2");
        long stopTime = System.currentTimeMillis();
        System.out.println("Group cache write speed test completed in " + (stopTime - startTime) + "ms");
    }

    public void testGroupCacheReadSpeed() throws JahiaInitializationException {
        Cache testCache = cacheService.createCacheInstance("testCache");
        for (int i=0; i < 100; i++) {
            Set groups = new HashSet();
            groups.add("group1");
            groups.add("group2");
            groups.add(Integer.toString(i));
            groups.add("group3");
            GroupCacheKey curKey = new GroupCacheKey(groups);
            testCache.put(curKey, Integer.toString(i));
        }
        long startTime = System.currentTimeMillis();
        for (int i=0; i < 50000; i++) {
            Set groups = new HashSet();
            groups.add("group1");
            groups.add("group2");
            groups.add(Integer.toString(50));
            groups.add("group3");
            GroupCacheKey curKey = new GroupCacheKey(groups);
            Object value = testCache.get(curKey);
        }
        long stopTime = System.currentTimeMillis();
        System.out.println("Group cache read speed test completed in " + (stopTime - startTime) + "ms");
    }
    
    public void testFillMemory() throws JahiaInitializationException {
    	System.out.println("Filling the memory with " + FILL_COUNT_ENTRIES + " entries... ");
        Cache testCache = cacheService.createCacheInstance("fillCache");
        for (int i=0; i < FILL_COUNT_ENTRIES; i++) {
            testCache.put("fill" + Integer.toString(i), new String("This is a reasonably long string so that we can try to fill the memory with it"));
            if (i % 2 == 0) {
            	// we perform a read so that we retrieve a reference to the object.
            	String value = (String) testCache.get("fill" + Integer.toString(i));
            }
        }
    }

    public void testGroupsFlushAfterMemoryFill() throws JahiaInitializationException {
        Cache testCache = cacheService.createCacheInstance("testCache");
        testCache.put("1", "1");
        testCache.put("2", "2");
        Set groups = new HashSet();
        groups.add("1");
        groups.add("3");
        GroupCacheKey groupKey1 = new GroupCacheKey(groups);
        System.out.println("groupKey1=" + groupKey1);
        testCache.put(groupKey1, "3");
        Object value2 = testCache.get("2");
        assertNotNull(value2);
        Object value3 = testCache.get(groupKey1);
        assertNotNull(value3);

        testFillMemory();

        testCache.flushGroup("1");
        Object value1 = testCache.get("1");
        assertNull(value1);
        value3 = testCache.get(groupKey1);
        assertNull(value3);
        testCache.put(groupKey1, "3");
        testCache.flushGroup("3");
        value3 = testCache.get(groupKey1);
        assertNull(value3);
    }


}
