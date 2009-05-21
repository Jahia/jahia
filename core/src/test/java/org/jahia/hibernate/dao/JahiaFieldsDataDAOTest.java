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
 * Copyright (c) 2005 Your Corpor
ion. All Rights Reserved.
 */
package org.jahia.hibernate.dao;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StopWatch;
import org.jahia.hibernate.model.JahiaObjectPK;

import java.util.List;
import java.util.Map;

public class JahiaFieldsDataDAOTest extends TestCase {
// ------------------------------ FIELDS ------------------------------

    JahiaFieldsDataDAO dao;
    protected ApplicationContext ctx = null;
    protected final Log log = LogFactory.getLog(getClass());

// --------------------------- CONSTRUCTORS ---------------------------

    public JahiaFieldsDataDAOTest() {
        String[] paths = {"spring/applicationContext-hibernate.xml","spring/applicationContext-dao.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }

// -------------------------- OTHER METHODS --------------------------

    public void setUp() throws Exception {
        super.setUp();
        dao = (JahiaFieldsDataDAO) ctx.getBean("jahiaFieldsDataDAO");
        assertNotNull(dao);
    }

    public void testFindAllFieldsIdInContainer() throws Exception {
        List list = dao.findAllFieldsIdInContainer(new Integer(2));
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testFindPublishedFieldsIdInContainer() throws Exception {
        List list = dao.findPublishedFieldsIdInContainer(new Integer(2));
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testFindStagingFieldsIdInContainer() throws Exception {
        List list = dao.findStagingFieldsIdInContainer(new Integer(2));
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testFindVersionedFieldsIdInContainer() throws Exception {
        List list = dao.findVersionedFieldsIdInContainer(new Integer(2), new Integer(999999999));
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetAllFieldsIdsFromContainerForPage() throws Exception {
        Map map = dao.getAllFieldsIdsFromContainerForPage(new Integer(1));
        assertNotNull(map);
        assertFalse(map.isEmpty());
    }

    public void testFindMetadataByOwnerAndName() throws Exception {
        StopWatch stopWatch = new StopWatch("testFindMetadataByOwnerAndName");
        for(int i = 1; i<2;i++) {
            stopWatch.start("dao.findMetadataByOwnerAndName(\"creator\",new JahiaObjectPK(\"ContentPage\",new Integer("+i+")))");
            Integer id = dao.findMetadataByOwnerAndName("lastPublishingDate",new JahiaObjectPK("ContentPage",new Integer(i)));
            System.out.println("id = " + id);
            stopWatch.stop();
            assertNotNull(id);
        }
        System.out.println("stopWatch.prettyPrint() = " + stopWatch.prettyPrint());
    }

    public void testFindJahiaObjectPKByMetadata() throws Exception {
        StopWatch stopWatch = new StopWatch("testFindMetadataByOwnerAndName");
        for(int i = 100; i<200;i++) {
            stopWatch.start("dao.findJahiaObjectPKByMetadata(,new Integer("+i+"))");
            JahiaObjectPK id = dao.findJahiaObjectPKByMetadata(new Integer(i));
            stopWatch.stop();
//            assertNotNull(id);
        }
        System.out.println("stopWatch.prettyPrint() = " + stopWatch.prettyPrint());
    }

    public void testGetSubPageId() throws Exception {
        dao.getSubPageId(new Integer(150), new Integer(2));
    }
}