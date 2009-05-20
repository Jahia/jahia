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