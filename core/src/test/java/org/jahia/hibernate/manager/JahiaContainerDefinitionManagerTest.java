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
 * Copyright (c) 2005 Your Cor
ration. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.containers.JahiaContainerSubDefinition;
import org.jmock.cglib.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.context.ApplicationContext;

import java.util.*;

public class JahiaContainerDefinitionManagerTest extends MockObjectTestCase {
// ------------------------------ FIELDS ------------------------------

    JahiaContainerDefinitionManager manager;
    protected ApplicationContext ctx = null;
    protected final Log log = LogFactory.getLog(getClass());

// --------------------------- CONSTRUCTORS ---------------------------

    public JahiaContainerDefinitionManagerTest() {
        log.debug("initialize test");
        ctx = SpringContextSingleton.getInstance().getContext();
    }

// -------------------------- OTHER METHODS --------------------------

    public void setUp() throws Exception {
        super.setUp();
//        File file = new File(new URI(Thread.currentThread().getContextClassLoader().getResource("config/jahia.properties").toExternalForm()));
//        String absolutePath = file.getAbsolutePath();
//        SettingsBean settingsBean = new SettingsBean(new FilePathResolver(file.getCanonicalPath()+File.separator+".."+File.separator+".."), absolutePath,
//                                                     "config/license.xml",1);
//        settingsBean.load();
//        JahiaMBeanServer.getInstance().init(settingsBean);
//        ServicesRegistry.getInstance().init(settingsBean);
        manager = (JahiaContainerDefinitionManager) ctx.getBean(JahiaContainerDefinitionManager.class.getName());
        assertNotNull(manager);
    }

    public void testCreateContainerDefinition() throws Exception {
        Map subdefs = new FastHashMap(3);
        List list = new ArrayList(3);
        Mock mockContainerStructure = new Mock(JahiaContainerStructure.class);
        JahiaContainerStructure containerStructure = (JahiaContainerStructure) mockContainerStructure.proxy();
        mockContainerStructure.expects(once()).method("getObjectType").withNoArguments().will(returnValue(2));
        mockContainerStructure.expects(once()).method("getObjectDefID").withNoArguments().will(returnValue(2));
        mockContainerStructure.expects(once()).method("getRank").withNoArguments().will(returnValue(0));
        list.add(containerStructure);
        Mock mockContainerSubDefinition = new Mock(JahiaContainerSubDefinition.class);
        JahiaContainerSubDefinition subDefinition = (JahiaContainerSubDefinition) mockContainerSubDefinition.proxy();
        mockContainerSubDefinition.expects(once()).method("getTitle").withNoArguments().will(returnValue("Test Cedric"));
        mockContainerSubDefinition.expects(atLeastOnce()).method("getID").withNoArguments().will(returnValue(0));
        mockContainerSubDefinition.expects(atLeastOnce()).method("getStructure").withNoArguments().will(returnValue(list));
        subdefs.put(new Integer(10),subDefinition);
        Mock mockContainerDefinition = new Mock(JahiaContainerDefinition.class);
        JahiaContainerDefinition containerDefinition = (JahiaContainerDefinition) mockContainerDefinition.proxy();
        mockContainerDefinition.expects(atLeastOnce()).method("getSubDefs").withNoArguments().will(returnValue(subdefs));
        mockContainerDefinition.expects(atLeastOnce()).method("getJahiaID").withNoArguments().will(returnValue(1));
        mockContainerDefinition.expects(once()).method("getName").withNoArguments().will(returnValue("Test Cedric"));
        mockContainerDefinition.expects(once()).method("setID").with(ANYTHING);
        manager.createContainerDefinition(containerDefinition);
        mockContainerDefinition.verify();
        mockContainerStructure.verify();
        mockContainerSubDefinition.verify();
        JahiaContainerDefinition jahiaContainerDefinition = manager.loadContainerDefinition(1,"Test Cedric");
        assertNotNull(jahiaContainerDefinition);
    }

    public void testUpdateContainerDefinition() throws Exception {
        JahiaContainerDefinition jahiaContainerDefinition = manager.loadContainerDefinition(10);
        assertNotNull(jahiaContainerDefinition);
        List List = new ArrayList(1);
        List.add("Test Cedric");
        jahiaContainerDefinition.composeStructure(List);
        manager.updateContainerDefinition(jahiaContainerDefinition);
        jahiaContainerDefinition = manager.loadContainerDefinition(10);
        assertNotNull(jahiaContainerDefinition);
    }

    public void testFullyLoadContainerDefinitionInTemplate() throws Exception {
        List ctnDef = manager.loadContainerDefinitionInTemplate(10);
        assertNotNull(ctnDef);
        assertTrue(ctnDef.size()>0);
    }

    public void testLoadContainerDefinitionInTemplate() throws Exception {
        List ctnDef = manager.loadContainerDefinitionInTemplate(13);
        assertNotNull(ctnDef);
        assertTrue(ctnDef.size()>0);
    }
}