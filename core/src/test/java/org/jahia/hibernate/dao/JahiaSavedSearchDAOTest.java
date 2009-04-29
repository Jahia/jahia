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
 package org.jahia.hibernate.dao;
/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 12 mai 2006
 * Time: 16:27:39
 * To change this template use File | Settings | File Templates.
 */

import junit.framework.TestCase;
import org.jboss.logging.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class JahiaSavedSearchDAOTest extends TestCase {
    JahiaSavedSearchDAO dao;
    private Logger log = Logger.getLogger(JahiaFieldsDefinitionDAOTest.class);
    private ApplicationContext ctx;

    public JahiaSavedSearchDAOTest() {
        String[] paths = {"spring/applicationContext-hibernate.xml","spring/applicationContext-dao.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }
    public void setUp() throws Exception {
        super.setUp();
        dao = (JahiaSavedSearchDAO) ctx.getBean("jahiaSavedSearchDAO");
        assertNotNull(dao);
    }
    public void testDeleteAllFromSite() throws Exception {
        dao.deleteAllFromSite(new Integer(100));
    }
}