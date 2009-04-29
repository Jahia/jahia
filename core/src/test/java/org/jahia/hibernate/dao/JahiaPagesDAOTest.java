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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

/**
 * JahiaContainerListDAO Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>01/06/2005</pre>
 */
public class JahiaPagesDAOTest extends MockObjectTestCase {
// ------------------------------ FIELDS ------------------------------

    protected ApplicationContext ctx = null;
    protected final Log log = LogFactory.getLog(JahiaPagesDAOTest.class);
    private final Integer version = new Integer(999999999);
    private JahiaPagesDAO dao = null;

// --------------------------- CONSTRUCTORS ---------------------------

    public JahiaPagesDAOTest() {
        String[] paths = {"spring/applicationContext-hibernate.xml","spring/applicationContext-dao.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }

// -------------------------- OTHER METHODS --------------------------

    public void setUp() throws Exception {
        super.setUp();
        dao = (JahiaPagesDAO) ctx.getBean("jahiaPagesDAO");
        assertNotNull(dao);
    }

    public void tearDown() throws Exception {
        super.tearDown();
        dao = null;
        ctx = null;
    }

    public void testgetPagePropertiesByValue() throws Exception {
        List list = dao.getPagePropertiesByValue("press-release");
        assertNotNull(list);
        assertTrue(list.size()>0);
    }

    public void testGetPagePropertiesByValueAndSiteID() throws Exception {
        List list = dao.getPagePropertiesByValueAndSiteID("dédé",new Integer(1));
        assertNotNull(list);
        assertTrue(list.size()==1);
    }
}

