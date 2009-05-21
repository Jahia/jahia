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

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.hibernate.dao.JahiaContainerListDAOTest;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JahiaContainerManagerTest extends TestCase {
    JahiaContainerManager manager;
    protected ApplicationContext ctx = null;
    protected final Log log = LogFactory.getLog(JahiaContainerListDAOTest.class);

    public JahiaContainerManagerTest() {
        String[] paths = {"spring/applicationContext*.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }

    public void setUp() throws Exception {
        super.setUp();
        manager = (JahiaContainerManager) ctx.getBean("jahiaContainerManager");
        assertNotNull(manager);
    }

    public void testLoadAllActiveContainerInPage() throws Exception {
        List list = manager.loadAllActiveContainerInPage(1);
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertTrue(list.get(0) instanceof JahiaContainer);
        log.debug("Number of containers found in page 1 : " + list.size());
    }

    public void testValidateStagedContainer() throws Exception {
        JahiaSaveVersion saveVersion = new JahiaSaveVersion(true, true, 99999999);
        JahiaContainer container = manager.saveContainer(new JahiaContainer(0, 1, 5, 12, 0, 28, 14, saveVersion.getVersionID(),
                                                                            EntryLoadRequest.STAGING_WORKFLOW_STATE),
                                                         saveVersion);
        Integer newId = new Integer(container.getID());
        List list = manager.getStagedContainerInPage(5);
        assertFalse(list.isEmpty());
        for (int i = 0; i < list.size(); i++) {
            Integer integer = (Integer) list.get(i);
            manager.validateStagedContainer(integer.intValue(), saveVersion);
        }
        list = null;
        assertNull(list);
        list = manager.getStagedContainerInPage(5);
        assertTrue(list.isEmpty());
        list = manager.getContainerIdsInContainerList(12,
                                                      new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE,
                                                                           saveVersion.getVersionID(), null), false);
        assertFalse(list.isEmpty());
        assertTrue(list.contains(newId));
    }

    public void testMultipleValidateStagedContainer() throws Exception {
        JahiaSaveVersion saveVersion = new JahiaSaveVersion(true, true, 99999999);
        List ids = new ArrayList();
        for (int i = 0; i < 100; i++) {
            JahiaContainer container = manager.saveContainer(new JahiaContainer(0, 1, 5, 12, 0, 28, 14,
                                                                                saveVersion.getVersionID(),
                                                                                EntryLoadRequest.STAGING_WORKFLOW_STATE),
                                                             saveVersion);
            Integer newId = new Integer(container.getID());
            ids.add(newId);
        }
        long startValidation = System.currentTimeMillis();
        log.info("Starting Validation");
        List list = manager.getStagedContainerInPage(5);
        assertFalse(list.isEmpty());
        for (int i = 0; i < list.size(); i++) {
            Integer integer = (Integer) list.get(i);
            manager.validateStagedContainer(integer.intValue(), saveVersion);
        }
        log.info("End of validation, operation takes "+(System.currentTimeMillis()-startValidation)+" ms");
        list = null;
        assertNull(list);
        list = manager.getStagedContainerInPage(5);
        assertTrue(list.isEmpty());
        list = manager.getContainerIdsInContainerList(12,
                                                      new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE,
                                                                           saveVersion.getVersionID(), null), false);
        assertFalse(list.isEmpty());
        for (int i = 0; i < ids.size(); i++) {
            Integer integer = (Integer) ids.get(i);
            assertTrue(list.contains(integer));
        }
    }

    public void testPurgeContainer() throws Exception {
        JahiaSaveVersion saveVersion = new JahiaSaveVersion(true, true, 99999999);
        JahiaContainer container = manager.saveContainer(new JahiaContainer(0, 1, 5, 12, 0, 28, 14, saveVersion.getVersionID(),
                                                                            EntryLoadRequest.STAGING_WORKFLOW_STATE),
                                                         saveVersion);
        Integer newId = new Integer(container.getID());
        manager.purgeContainer(newId.intValue());
        List list = manager.getContainerIdsInContainerList(12, null, false);
        assertFalse(list.contains(newId));
    }

    public void testDeleteContainer() throws Exception {
        List list = manager.getPublishedContainerInPage(5);
        JahiaSaveVersion saveVersion = new JahiaSaveVersion(true, true, new Random().nextInt());
        for (int i = 0; i < list.size(); i++) {
            Integer id = (Integer) list.get(i);
            manager.deleteContainer(id.intValue(), saveVersion);
            manager.validateStagedContainer(id.intValue(), saveVersion);
        }
        assertTrue(manager.getPublishedContainerInPage(5).isEmpty());
    }
}