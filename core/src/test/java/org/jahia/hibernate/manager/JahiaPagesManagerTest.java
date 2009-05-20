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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.services.pages.PageProperty;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JahiaPagesManagerTest extends MockObjectTestCase {
    private JahiaPagesManager manager;
    private Log log = LogFactory.getLog(JahiaPagesManagerTest.class);
    private ClassPathXmlApplicationContext ctx;

    public JahiaPagesManagerTest() {
        super();
        String[] paths = {"spring/applicationContext*.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }

    protected void setUp() throws Exception {
        super.setUp();
        manager = (JahiaPagesManager) ctx.getBean(JahiaPagesManager.class.getName());
        assertNotNull(manager);
    }

    public void testGetPagePropertiesByValue() throws Exception {
        List list = manager.getPagePropertiesByValue("press-release");
        assertNotNull(list);
        assertTrue(list.size()>0);
        assertTrue(list.get(0) instanceof PageProperty);
    }
}