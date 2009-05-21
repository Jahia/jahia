/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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