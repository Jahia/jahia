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
 * Date: 25 avr. 2006
 * Time: 15:50:03
 * To change this template use File | Settings | File Templates.
 */

import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JahiaAuditLogDAOTest extends TestCase {
    protected final Log log = LogFactory.getLog(getClass());
    JahiaAuditLogDAO jahiaAuditLogDAO;
    protected ApplicationContext ctx = null;

    public JahiaAuditLogDAOTest() {
        String[] paths = {"spring/applicationContext-hibernate.xml","spring/applicationContext-dao.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }
    public void setUp() throws Exception {
        super.setUp();
        jahiaAuditLogDAO = (JahiaAuditLogDAO) ctx.getBean("jahiaAuditLogDAO");
        assertNotNull(jahiaAuditLogDAO);
    }
    public void testDeleteOldestRow() throws Exception {
        jahiaAuditLogDAO.deleteOldestRow();
    }
}