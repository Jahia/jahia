package org.jahia.services.templates;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jahia.bin.Action;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
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
                FileUtils.deleteQuietly(new File(settingsBean.getJahiaTemplatesDiskPath(), "dummy1"));
                FileUtils.deleteQuietly(new File(settingsBean.getJahiaSharedTemplatesDiskPath(), "dummy1-1.0-SNAPSHOT.war"));

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
                File deployedTemplatesFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), managerService.getTemplatePackageByFileName("jahia-test-module-war").getRootFolderWithVersion());
                managerService.deployModule(new File(deployedTemplatesFolder, "resources/dummy1-1.0-SNAPSHOT.war"), session);

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
        File deployedTemplatesFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), managerService.getTemplatePackageByFileName("jahia-test-module-war").getRootFolderWithVersion());

        try {
            FileUtils.copyFileToDirectory(new File(deployedTemplatesFolder, "resources/dummy1-1.0-SNAPSHOT.war"), new File(settingsBean.getJahiaSharedTemplatesDiskPath()));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        managerService.scanSharedModulesFolderNow();

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
                File deployedTemplatesFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), managerService.getTemplatePackageByFileName("jahia-test-module-war").getRootFolderWithVersion());
                JahiaTemplatesPackage pack = managerService.deployModule(new File(deployedTemplatesFolder, "resources/dummy1-1.0-SNAPSHOT.war"), session);
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

    @Test
    public void testDefinitionUpdate() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                SettingsBean settingsBean = SettingsBean.getInstance();
                File deployedTemplatesFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), managerService.getTemplatePackageByFileName("jahia-test-module-war").getRootFolderWithVersion());
                JahiaTemplatesPackage pack = managerService.deployModule(new File(deployedTemplatesFolder, "resources/dummy1-1.0-SNAPSHOT.war"), session);

                File def = new File(settingsBean.getJahiaTemplatesDiskPath(), pack.getRootFolderWithVersion() + "/META-INF/definitions.cnd");
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
    }

    @Test
    public void testContextUpdate() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                SettingsBean settingsBean = SettingsBean.getInstance();
                File deployedTemplatesFolder = new File(settingsBean.getJahiaTemplatesDiskPath(), managerService.getTemplatePackageByFileName("jahia-test-module-war").getRootFolderWithVersion());
                JahiaTemplatesPackage pack = managerService.deployModule(new File(deployedTemplatesFolder, "resources/dummy1-1.0-SNAPSHOT.war"), session);

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
                JahiaTemplatesPackage pack = managerService.deployModule(new File(deployedTemplatesFolder, "resources/dummy1-1.0-SNAPSHOT.war"), session);

                TestInitializingBean.resetInstance();

                File spring = new File(settingsBean.getJahiaTemplatesDiskPath(), pack.getRootFolderWithVersion() + "/META-INF/spring/dummy1.xml");
                File newspring = new File(settingsBean.getJahiaTemplatesDiskPath(), pack.getRootFolderWithVersion() + "/META-INF/spring/dummy1-modified-2.xml");
                try {
                    SAXReader reader = new SAXReader();
                    Document document = reader.read(spring);

                    Element newBean = document.getRootElement().addElement("bean");
                    newBean.addAttribute("class", "org.jahia.services.templates.TestInitializingBean");
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
    }


}
