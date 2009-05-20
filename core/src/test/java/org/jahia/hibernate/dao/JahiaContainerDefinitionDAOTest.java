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
import org.jahia.hibernate.model.JahiaCtnDef;
import org.jahia.hibernate.model.JahiaCtnDefProperty;
import org.jahia.hibernate.model.JahiaCtnStruct;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;
import java.util.Set;

public class JahiaContainerDefinitionDAOTest extends TestCase {
    JahiaContainerDefinitionDAO dao;
    protected ApplicationContext ctx = null;
    protected final Log log = LogFactory.getLog(getClass());

// --------------------------- CONSTRUCTORS ---------------------------

    public JahiaContainerDefinitionDAOTest() {
        String[] paths = {"spring/applicationContext*.xml"};
        log.debug("initialize test");
        ctx = new ClassPathXmlApplicationContext(paths);
    }

// -------------------------- OTHER METHODS --------------------------

    public void setUp() throws Exception {
        super.setUp();
        dao = (JahiaContainerDefinitionDAO) ctx.getBean("jahiaContainerDefinitionDAO");
        assertNotNull(dao);
    }

    public void testFullyLoadContainerDefinition() throws Exception {
        JahiaCtnDef ctnDef = dao.fullyLoadContainerDefinition(new Integer(10));
        assertNotNull(ctnDef);
        assertFalse(ctnDef.getSubDefinitions().isEmpty());
        assertFalse(((JahiaCtnDefProperty) ctnDef.getSubDefinitions().iterator().next()).getJahiaCtnStructs().isEmpty());
    }

    public void testFullyLoadContainerDefinitionInTemplate() throws Exception {
        List ctnDef = dao.fullyLoadContainerDefinitionInTemplateForTestingPurpose(new Integer(13));  // loadHomepageDefinition
        assertNotNull(ctnDef);
        assertTrue(ctnDef.size()>0);
        for (int i = 0; i < 1; i++) {
            JahiaCtnDef jahiaCtnDef = (JahiaCtnDef) ctnDef.get(i);
            dao.update(jahiaCtnDef);
        }
    }

    public void testSaveContainerDefinition() throws Exception {
        JahiaCtnDef ctnDef = dao.fullyLoadContainerDefinitionForTestingPurpose(new Integer(10));
        assertNotNull(ctnDef);
        Set subDefinitions = ctnDef.getSubDefinitions();
        assertFalse(subDefinitions.isEmpty());
        Set jahiaCtnStructs = ((JahiaCtnDefProperty) subDefinitions.iterator().next()).getJahiaCtnStructs();
        assertFalse(jahiaCtnStructs.isEmpty());
        JahiaCtnStruct struct = (JahiaCtnStruct) jahiaCtnStructs.iterator().next();
        log.debug("properties size = "+jahiaCtnStructs.size());
        log.debug(struct);
        Integer rankJahiaCtnStruct = new Integer(struct.getRankJahiaCtnStruct().intValue()+1);
        struct.setRankJahiaCtnStruct(rankJahiaCtnStruct);
//        ctnDef.getSubDefinitions().clear();
        jahiaCtnStructs.add(struct);
        dao.update(ctnDef);
        ctnDef = null;
        ctnDef = dao.fullyLoadContainerDefinitionForTestingPurpose(new Integer(10));
        subDefinitions = ctnDef.getSubDefinitions();
        assertNotNull(ctnDef);
        assertFalse(subDefinitions.isEmpty());
        log.debug("properties size = "+subDefinitions.size());
        jahiaCtnStructs = ((JahiaCtnDefProperty) subDefinitions.iterator().next()).getJahiaCtnStructs();
        struct = (JahiaCtnStruct) jahiaCtnStructs.iterator().next();
        log.debug(struct);
        assertEquals(rankJahiaCtnStruct,struct.getRankJahiaCtnStruct());
    }
}