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
/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import org.apache.log4j.Logger;
import org.jahia.services.fields.ContentField;
import org.jahia.services.version.ContentObjectEntryState;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Iterator;
import java.util.Map;

public class JahiaFieldsDataManagerTest extends MockObjectTestCase {
    JahiaFieldsDataManager manager;
    private ClassPathXmlApplicationContext ctx;
    private Logger log = Logger.getLogger(getClass());

    public JahiaFieldsDataManagerTest() {
        String[] paths = {"spring/applicationContext*.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }
    public void setUp() throws Exception {
        super.setUp();
        manager = (JahiaFieldsDataManager) ctx.getBean(JahiaFieldsDataManager.class.getName());
        assertNotNull(manager);
    }
    public void testLoadAllValues() throws Exception {
        Map map = manager.loadAllValues(6);
        assertFalse(map.isEmpty());
        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            assertTrue(entry.getKey() instanceof ContentObjectEntryState);
            assertTrue(entry.getValue() instanceof String);
            assertTrue(entry.getValue().equals("Welcome to Jahia"));
        }
    }

    public void testLoadContentField() throws Exception {
        ContentField contentField = manager.loadContentField(6);
        assertNotNull(contentField);
        assertTrue(contentField.hasActiveEntries());
        contentField = manager.loadContentField(6);
        assertNotNull(contentField);
        assertTrue(contentField.hasActiveEntries());
        contentField = manager.loadContentField(7);
        assertNotNull(contentField);
        assertTrue(contentField.hasActiveEntries());
        contentField = manager.loadContentField(7);
        assertNotNull(contentField);
        assertTrue(contentField.hasActiveEntries());
        contentField = manager.loadContentField(6);
        assertNotNull(contentField);
        assertTrue(contentField.hasActiveEntries());
    }

    public void testGetFieldsIdsInContainer() throws Exception {
        fail("Test not implemented yet");
    }
}