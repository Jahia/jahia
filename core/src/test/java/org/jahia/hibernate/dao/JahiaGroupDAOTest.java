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

import java.util.ArrayList;
import java.util.List;

public class JahiaGroupDAOTest extends MockObjectTestCase {
    JahiaGroupDAO dao;
    private Log log = LogFactory.getLog(JahiaLinkDAOTest.class);
    private ApplicationContext ctx;

    public JahiaGroupDAOTest() {
        String[] paths = {"spring/applicationContext*.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }
    public void setUp() throws Exception {
        super.setUp();
        dao = (JahiaGroupDAO) ctx.getBean("jahiaGroupDAO");
        assertNotNull(dao);
    }
    public void testSearchGroupName() throws Exception {
        List list = dao.searchGroupName("%",new Integer(1));
        assertTrue(list.size()>0);
        for (int i = 0; i < list.size(); i++) {
            String o = (String) list.get(i);
            log.debug(o);

        }
        List names = new ArrayList(1);
        names.add("*");
        List values = new ArrayList(1);
        values.add("%");
        list = null;
        assertNull(list);
        list = dao.searchGroupName(names, values, new Integer(1), "db");
        assertTrue(list.size()>0);
        for (int i = 0; i < list.size(); i++) {
            String o = (String) list.get(i);
            log.debug(o);

        }
    }
}