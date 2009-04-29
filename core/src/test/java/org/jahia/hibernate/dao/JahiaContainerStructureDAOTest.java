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
package org.jahia.hibernate.dao;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JahiaContainerStructureDAOTest extends TestCase {
    protected JahiaContainerStructureDAO dao;
    protected ApplicationContext ctx = null;
    protected final Log log = LogFactory.getLog(getClass());

    public JahiaContainerStructureDAOTest() {
        String[] paths = {"spring/applicationContext*.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dao = (JahiaContainerStructureDAO) ctx.getBean("jahiaContainerStructureDAO");
        assertNotNull(dao);
    }

    public void testFindContainerDefinitionParents() throws Exception {
        boolean hasContainerDefinitionParents = dao.hasContainerDefinitionParents(new Integer(14));
        assertTrue(hasContainerDefinitionParents);
    }
}