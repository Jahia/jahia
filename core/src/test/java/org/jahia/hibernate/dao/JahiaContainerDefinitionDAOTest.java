/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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