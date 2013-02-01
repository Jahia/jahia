/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.test.services.templates;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jahia.api.Constants;
import org.jahia.bin.Action;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.Assert.*;

public class ModuleDeploymentTest {

    private static JahiaTemplateManagerService managerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
    
    private static final String VERSION = Constants.JAHIA_PROJECT_VERSION;

    @Before
    @After
    public void clearDeployedModule() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JahiaTemplatesPackage oldpack = managerService.getTemplatePackageByFileName("dummy1");
                if (oldpack != null) {
                    managerService.undeployModule(oldpack, session);
                }
                SettingsBean settingsBean = SettingsBean.getInstance();
                FileUtils.deleteQuietly(new File(settingsBean.getJahiaModulesDiskPath(), "dummy1-" + VERSION + ".war"));

                return null;
            }
        });
    }

    @Test
    public void testWarDeploy() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                SettingsBean settingsBean = SettingsBean.getInstance();
                File deployedTemplatesFolder = new File(settingsBean.getJahiaModulesDiskPath(), managerService.getTemplatePackageByFileName("jahia-test-module-war").getRootFolderWithVersion());
                managerService.deployModule(new File(deployedTemplatesFolder, "resources/dummy1-" + VERSION + ".war"), session);

                JahiaTemplatesPackage pack = managerService.getTemplatePackageByFileName("dummy1");
                assertNotNull(pack);
                assertNotNull("Spring context is null", pack.getContext());
                assertTrue("No action defined", !pack.getContext().getBeansOfType(Action.class).isEmpty());
                assertTrue("Action not registered", managerService.getActions().containsKey("my-post-action"));

                try {
                    NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent1");
                } catch (NoSuchNodeTypeException e) {
                    fail("Definition not registered");
                }
                assertTrue("Module view is not correctly registered", managerService.getModulesWithViewsForComponent("jnt:testComponent1").contains(pack));

                return null;
            }
        });
    }

    @Test
    public void testWarAutoDeploy() throws RepositoryException {
        SettingsBean settingsBean = SettingsBean.getInstance();
        File deployedTemplatesFolder = new File(settingsBean.getJahiaModulesDiskPath(), managerService.getTemplatePackageByFileName("jahia-test-module-war").getRootFolderWithVersion());

        try {
            FileUtils.copyFileToDirectory(new File(deployedTemplatesFolder, "resources/dummy1-" + VERSION + ".war"), new File(settingsBean.getJahiaModulesDiskPath()));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        //managerService.scanSharedModulesFolderNow();

        JahiaTemplatesPackage pack = managerService.getTemplatePackageByFileName("dummy1");
        assertNotNull(pack);
        assertNotNull("Spring context is null", pack.getContext());
        assertTrue("No action defined", !pack.getContext().getBeansOfType(Action.class).isEmpty());
        assertTrue("Action not registered", managerService.getActions().containsKey("my-post-action"));

        try {
            NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent1");
        } catch (NoSuchNodeTypeException e) {
            fail("Definition not registered");
        }
        assertTrue("Module view is not correctly registered", managerService.getModulesWithViewsForComponent("jnt:testComponent1").contains(pack));
    }

    @Test
    public void testWarUndeploy() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                SettingsBean settingsBean = SettingsBean.getInstance();
                File deployedTemplatesFolder = new File(settingsBean.getJahiaModulesDiskPath(), managerService.getTemplatePackageByFileName("jahia-test-module-war").getRootFolderWithVersion());
                JahiaTemplatesPackage pack = managerService.deployModule(new File(deployedTemplatesFolder, "resources/dummy1-" + VERSION + ".war"), session);
                managerService.undeployModule(pack, session);

                pack = managerService.getTemplatePackageByFileName("dummy1");
                assertNull(pack);
                assertFalse("Action not unregistered", managerService.getActions().containsKey("my-post-action"));
                try {
                    NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent1");
                    fail("Definition not unregistered");
                } catch (NoSuchNodeTypeException e) {
                }
                assertTrue("Module view is not correctly unregistered", managerService.getModulesWithViewsForComponent("jnt:testComponent1").isEmpty());

                return null;
            }
        });
    }

    /*@Test
    public void testDefinitionUpdate() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                SettingsBean settingsBean = SettingsBean.getInstance();
                File deployedTemplatesFolder = new File(settingsBean.getJahiaModulesDiskPath(), managerService.getTemplatePackageByFileName("jahia-test-module-war").getRootFolderWithVersion());
                JahiaTemplatesPackage pack = managerService.deployModule(new File(deployedTemplatesFolder, "resources/dummy1-" + VERSION + ".war"), session);

                File def = new File(settingsBean.getJahiaModulesDiskPath(), pack.getRootFolderWithVersion() + "/META-INF/definitions.cnd");
                try {
                    BufferedWriter w = new BufferedWriter(new FileWriter(def, true));
                    w.write("[jnt:testComponent2] > jnt:content, jmix:layoutComponentContent\n");
                    w.write(" - property (string)\n");
                    w.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                managerService.scanSharedModulesFolderNow();

                pack = managerService.getTemplatePackageByFileName("dummy1");

                assertNotNull("Spring context is null", pack.getContext());
                assertTrue("No action defined", !pack.getContext().getBeansOfType(Action.class).isEmpty());
                assertTrue("Action not registered", managerService.getActions().containsKey("my-post-action"));

                try {
                    NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent1");
                    NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent2");
                } catch (NoSuchNodeTypeException e) {
                    fail("Definition not registered");
                }
                assertTrue("Module view is not correctly registered", managerService.getModulesWithViewsForComponent("jnt:testComponent1").contains(pack));

                return null;
            }
        });
    }*/

    /*@Test
    public void testContextUpdate() throws IOException, RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                SettingsBean settingsBean = SettingsBean.getInstance();
                File deployedTemplatesFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), managerService.getTemplatePackageByFileName("jahia-test-module-war").getRootFolderWithVersion());
                JahiaTemplatesPackage pack = managerService.deployModule(new File(deployedTemplatesFolder, "resources/dummy1-" + VERSION + ".war"), session);

                File spring = new File(settingsBean.getJahiaTemplatesDiskPath(), pack.getRootFolderWithVersion() + "/META-INF/spring/dummy1.xml");
                File newspring = new File(settingsBean.getJahiaTemplatesDiskPath(), pack.getRootFolderWithVersion() + "/META-INF/spring/dummy1-modified.xml");
                try {
                    SAXReader reader = new SAXReader();
                    Document document = reader.read(spring);

                    Element newBean = document.getRootElement().addElement("bean");
                    newBean.addAttribute("class", "org.jahia.bin.DefaultPostAction");
                    Element prop = newBean.addElement("property");
                    prop.addAttribute("name","name");
                    prop.addAttribute("value","my-new-post-action");
                    spring.delete();
                    XMLWriter writer = new XMLWriter(new FileWriter(newspring), OutputFormat.createPrettyPrint());
                    writer.write(document);
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                managerService.scanSharedModulesFolderNow();

                pack = managerService.getTemplatePackageByFileName("dummy1");

                assertNotNull("Spring context is null", pack.getContext());
                assertTrue("No action defined", !pack.getContext().getBeansOfType(Action.class).isEmpty());
                assertTrue("Action not registered", managerService.getActions().containsKey("my-post-action"));
                assertTrue("New action not registered", managerService.getActions().containsKey("my-new-post-action"));

                try {
                    NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent1");
                } catch (NoSuchNodeTypeException e) {
                    fail("Definition not registered");
                }
                assertTrue("Module view is not correctly registered", managerService.getModulesWithViewsForComponent("jnt:testComponent1").contains(pack));

                return null;
            }
        });
    }

    @Test
    public void testContextUpdateWithInitializingBean() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                SettingsBean settingsBean = SettingsBean.getInstance();
                File deployedTemplatesFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), managerService.getTemplatePackageByFileName("jahia-test-module-war").getRootFolderWithVersion());
                JahiaTemplatesPackage pack = managerService.deployModule(new File(deployedTemplatesFolder, "resources/dummy1-" + VERSION + ".war"), session);

                TestInitializingBean.resetInstance();

                File spring = new File(settingsBean.getJahiaTemplatesDiskPath(), pack.getRootFolderWithVersion() + "/META-INF/spring/dummy1.xml");
                File newspring = new File(settingsBean.getJahiaTemplatesDiskPath(), pack.getRootFolderWithVersion() + "/META-INF/spring/dummy1-modified-2.xml");
                try {
                    SAXReader reader = new SAXReader();
                    Document document = reader.read(spring);

                    Element newBean = document.getRootElement().addElement("bean");
                    newBean.addAttribute("class", TestInitializingBean.class.getName());
                    spring.delete();
                    XMLWriter writer = new XMLWriter(new FileWriter(newspring), OutputFormat.createPrettyPrint());
                    writer.write(document);
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                managerService.scanSharedModulesFolderNow();

                pack = managerService.getTemplatePackageByFileName("dummy1");

                assertNotNull("Spring context is null", pack.getContext());
                assertTrue("No action defined", !pack.getContext().getBeansOfType(Action.class).isEmpty());
                assertTrue("Action not registered", managerService.getActions().containsKey("my-post-action"));

                assertNotNull("Bean not instantiated",TestInitializingBean.getInstance());
                assertTrue("Bean not initialized",TestInitializingBean.getInstance().isInitialized());
                assertTrue("Bean not started",TestInitializingBean.getInstance().isStarted());
                try {
                    NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent1");
                } catch (NoSuchNodeTypeException e) {
                    fail("Definition not registered");
                }
                assertTrue("Module view is not correctly registered", managerService.getModulesWithViewsForComponent("jnt:testComponent1").contains(pack));

                managerService.undeployModule(pack, session);

                assertTrue("Bean stopped",TestInitializingBean.getInstance().isStopped());
                return null;
            }
        });
    }*/


}
