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

