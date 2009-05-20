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
 * Copyright (c) 2005 Your Corpo
 All Rights Reserved.
 */
package org.jahia.hibernate.dao;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.jahia.hibernate.model.JahiaContainer;
import org.jahia.hibernate.model.JahiaCtnEntryPK;

import java.util.List;

public class JahiaContainerDAOTest extends TestCase {
// ------------------------------ FIELDS ------------------------------

    protected ApplicationContext ctx = null;
    protected final Log log = LogFactory.getLog(JahiaContainerListDAOTest.class);
    private JahiaContainerDAO dao = null;
    private JahiaContainerDefinitionDAO definitionDAO = null;
    private final Integer version = Integer.MAX_VALUE;

// --------------------------- CONSTRUCTORS ---------------------------

    public JahiaContainerDAOTest() {
        String[] paths = {"spring/applicationContext*.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }

// -------------------------- OTHER METHODS --------------------------

    public void setUp() throws Exception {
        super.setUp();
        dao = (JahiaContainerDAO) ctx.getBean("jahiaContainerDAO");
        definitionDAO = (JahiaContainerDefinitionDAO) ctx.getBean("jahiaContainerDefinitionDAO");
        assertNotNull(dao);
    }

    public void testGetAllContainerIds() throws Exception {
        List list = dao.getAllContainerIds();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetAllContainerIdsFromList() throws Exception {
        List list = dao.getAllContainerIdsFromList(new Integer(2));
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetAllContainerIdsFromSite() throws Exception {
        List list = dao.getAllContainerIdsFromSite(new Integer(1));
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetAllNonDeletedStagingContainerIds() throws Exception {
        List list = dao.getAllNonDeletedStagingContainerIds();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetAllPublishedContainerIds() throws Exception {
        List list = dao.getAllPublishedContainerIds();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetAllStagingContainerIds() throws Exception {
        List list = dao.getAllStagingContainerIds();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetAllVersionedContainerIds() throws Exception {
        List list = dao.getAllVersionedContainerIds(version);
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetNonDeletedStagingContainerIdsFromList() throws Exception {
        List list = dao.getNonDeletedStagingContainerIdsFromList(new Integer(14));
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetPublishedContainerIdsFromList() throws Exception {
        List list = dao.getPublishedContainerIdsFromList(new Integer(14));
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetStagedContainerInPage() throws Exception {
        List list = dao.getStagedContainerInPage(new Integer(1));
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetStagingContainerIdsFromList() throws Exception {
        List list = dao.getStagingContainerIdsFromList(new Integer(14));
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetVersionedContainerIdsFromList() throws Exception {
        List list = dao.getVersionedContainerIdsFromList(new Integer(14),version);
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetAllContainerAclIdsFromSite() throws Exception {
        List list = dao.getAllContainerAclIdsFromSite(new Integer(1));
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetPublishedContainerIdsInListSortedByFieldValue() throws Exception {
        List list = dao.getPublishedContainerIdsInListSortedByFieldValue(new Integer(2),"subTitlemain_1",false);
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testGetAllContainerIdsInListSortedByFieldValue() throws Exception {
        List list = dao.getAllContainerIdsInListSortedByFieldValue(new Integer(2),"subTitlemain_1",false);
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    public void testSaveOrUpdate() throws Exception {
        JahiaContainer container = new JahiaContainer();
        container.setComp_id(new JahiaCtnEntryPK(null,new Integer(0),new Integer(2)));
        container.setCtndef(definitionDAO.findDefinitionById(new Integer(10)));
        container.setJahiaAclId(new Integer(28));
        container.setListid(new Integer(14));
        container.setPageid(new Integer(5));
        container.setRank(new Integer(0));
        container.setSiteId(new Integer(1));
        dao.save(container);
        final int id = container.getComp_id().getId().intValue();
        assertTrue(id>0);
        container.setRank(new Integer(1));
        dao.update(container);
        container = null;
        assertNull(container);
        container = dao.findContainerById(new Integer(id), new Integer(0), new Integer(2));
        assertTrue(container.getRank().intValue()==1);
    }

    public void testFindContainers() throws Exception {
        List list = dao.findContainers(new Integer(1));
        assertNotNull(list);
        assertFalse(list.isEmpty());
        list=null;
        list = dao.findContainers(new Integer(1));
        assertNotNull(list);
        assertFalse(list.isEmpty());
        assertTrue(list.get(0) instanceof JahiaContainer);
    }

    public void testDeleteActiveContainer() throws Exception {
        fail("Test not implemented yet");
    }
}