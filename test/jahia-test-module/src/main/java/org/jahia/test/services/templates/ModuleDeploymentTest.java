/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.test.services.templates;

import org.apache.commons.io.FileUtils;
import org.awaitility.Durations;
import org.jahia.bin.Action;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.FrameworkService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.with;
import static org.junit.Assert.*;

public class ModuleDeploymentTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ModuleDeploymentTest.class);

    private static JahiaTemplateManagerService managerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();

    private static final String TESTSITE_NAME = "ModuleDeploymentTestSite";
    private static final String DUMMY_1_MODULE = "dummy1";
    private static final String JAHIA_TEST_MODULE = "jahia-test-module";
    private static final String V_2_0_0_JAR = "-2.0.0.jar";

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
            TestHelper.createSite(TESTSITE_NAME);
        } catch (Exception e) {
            logger.error("Cannot create or publish site", e);
            fail();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            TestHelper.deleteSite(TESTSITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }



    @Before
    @After
    public void clearDeployedModule() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                logger.info("--- clear deployed modules");
                JahiaTemplatesPackage oldpack = managerService.getTemplatePackageRegistry().lookupByIdAndVersion(DUMMY_1_MODULE, new ModuleVersion("2.0.0"));
                if (oldpack != null) {
                    managerService.undeployModule(oldpack);
                }
                oldpack = managerService.getTemplatePackageRegistry().lookupByIdAndVersion(DUMMY_1_MODULE, new ModuleVersion("2.1.0"));
                if (oldpack != null) {
                    managerService.undeployModule(oldpack);
                }

                SettingsBean settingsBean = SettingsBean.getInstance();
                FileUtils.deleteQuietly(new File(settingsBean.getJahiaModulesDiskPath(), DUMMY_1_MODULE + V_2_0_0_JAR));
                FileUtils.deleteQuietly(new File(settingsBean.getJahiaModulesDiskPath(), DUMMY_1_MODULE + "-2.1.0.jar"));

                Bundle[] bundles = FrameworkService.getBundleContext().getBundles();
                for (Bundle bundle : bundles) {
                    if (bundle.getSymbolicName().equals(DUMMY_1_MODULE)) {
                        try {
                            bundle.uninstall();
                        } catch (BundleException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                return null;
            }
        });
    }

    @Test
    public void testJarDeploy() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                try {
                    File tmpFile = File.createTempFile("module",".jar");
                    InputStream stream = managerService.getTemplatePackageById(JAHIA_TEST_MODULE).getResource(DUMMY_1_MODULE + V_2_0_0_JAR).getInputStream();
                    FileUtils.copyInputStreamToFile(stream,  tmpFile);
                    managerService.deployModule(tmpFile, session);
                    tmpFile.delete();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    fail(e.toString());
                }

                JahiaTemplatesPackage pack = managerService.getTemplatePackageById(DUMMY_1_MODULE);
                assertNotNull(pack);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                assertEquals("Module is not started", ModuleState.State.STARTED, pack.getState().getState());
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
    public void testNewVersionDeploy() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    File tmpFile = File.createTempFile("module",".jar");
                    InputStream stream = managerService.getTemplatePackageById(JAHIA_TEST_MODULE).getResource(DUMMY_1_MODULE + V_2_0_0_JAR).getInputStream();
                    FileUtils.copyInputStreamToFile(stream,  tmpFile);
                    managerService.deployModule(tmpFile, session);
                    tmpFile.delete();
                    tmpFile = File.createTempFile("module",".jar");
                    stream = managerService.getTemplatePackageById(JAHIA_TEST_MODULE).getResource(DUMMY_1_MODULE + "-2.1.0.jar").getInputStream();
                    FileUtils.copyInputStreamToFile(stream,  tmpFile);
                    managerService.deployModule(tmpFile, session);
                    tmpFile.delete();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    fail(e.toString());
                }

                JahiaTemplatesPackage pack = managerService.getTemplatePackageById(DUMMY_1_MODULE);
                assertNotNull(pack);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                assertEquals("Module is not started", ModuleState.State.STARTED, pack.getState().getState());
                assertEquals("Active module version is incorrect", "2.1.0", pack.getVersion().toString());

                return null;
            }
        });
    }

    @Test
    public void testJarAutoDeploy() throws RepositoryException {
        SettingsBean settingsBean = SettingsBean.getInstance();

        File f = null;
        try {
            File tmpFile = File.createTempFile("module",".jar");
            InputStream stream = managerService.getTemplatePackageById(JAHIA_TEST_MODULE).getResource(DUMMY_1_MODULE + V_2_0_0_JAR).getInputStream();
            FileUtils.copyInputStreamToFile(stream,  tmpFile);
            FileUtils.copyFileToDirectory(tmpFile, new File(settingsBean.getJahiaModulesDiskPath()));
            tmpFile.delete();
            f = new File(settingsBean.getJahiaModulesDiskPath(), tmpFile.getName());

            with().pollInterval(Durations.ONE_SECOND).await().atMost(20, SECONDS).until(isPackageDeployedAndServiceInstalled(DUMMY_1_MODULE));

            JahiaTemplatesPackage pack = managerService.getTemplatePackageById(DUMMY_1_MODULE);

            assertNotNull(pack);
            assertEquals("Module is not started", ModuleState.State.STARTED, pack.getState().getState());
            assertNotNull("Spring context is null", pack.getContext());
            assertTrue("No action defined", !pack.getContext().getBeansOfType(Action.class).isEmpty());
            assertTrue("Action not registered", managerService.getActions().containsKey("my-post-action"));

            try {
                NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent1");
            } catch (NoSuchNodeTypeException e) {
                fail("Definition not registered");
            }
            assertTrue("Module view is not correctly registered", managerService.getModulesWithViewsForComponent("jnt:testComponent1").contains(pack));

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            fail(e.toString());
        } finally {
            FileUtils.deleteQuietly(f);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    @Test
    public void testJarWithFailingRules() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                File tmpFile = null;
                try {
                    tmpFile = File.createTempFile("module", ".jar");
                    FileUtils.copyURLToFile(managerService.getTemplatePackageById(JAHIA_TEST_MODULE)
                            .getResource(DUMMY_1_MODULE + V_2_0_0_JAR).getURL(), tmpFile);
                    Map<String, String> env = new HashMap<>();
                    env.put("create", "true");
                    try (FileSystem fs = FileSystems.newFileSystem(URI.create("jar:" + tmpFile.toURI().toURL()), env)) {
                        File droolsFile = File.createTempFile("rules", ".drl");
                        FileUtils.writeStringToFile(droolsFile, "dummy text");
                        Files.copy(droolsFile.toPath(), fs.getPath("/META-INF/rules.drl"), StandardCopyOption.REPLACE_EXISTING);
                        FileUtils.deleteQuietly(droolsFile);
                    }

                    JahiaTemplatesPackage pack = managerService.deployModule(tmpFile, session);

                    // we expect the module to be in ERROR_WITH_RULES and the bundle in RESOLVED state after deployment
                    with().pollInterval(Durations.ONE_SECOND).await().atMost(20, SECONDS).until(new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                            return pack.getState().getState() == ModuleState.State.ERROR_WITH_RULES
                                    && pack.getBundle().getState() == Bundle.RESOLVED;
                        }
                    });
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    fail(e.toString());
                } finally {
                    FileUtils.deleteQuietly(tmpFile);
                }
                return null;
            }
        });
    }

    private Callable<Boolean> isPackageDeployedAndServiceInstalled(final String packageId) {
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                JahiaTemplatesPackage pack = managerService.getTemplatePackageById(DUMMY_1_MODULE);
                return pack != null && pack.getContext() != null && pack.isServiceInitialized();
            }
        };
    }

    @Test
    public void testJarUndeploy() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    File tmpFile = File.createTempFile("module",".jar");
                    InputStream stream = managerService.getTemplatePackageById(JAHIA_TEST_MODULE).getResource(DUMMY_1_MODULE + V_2_0_0_JAR).getInputStream();
                    FileUtils.copyInputStreamToFile(stream,  tmpFile);
                    JahiaTemplatesPackage pack = managerService.deployModule(tmpFile, session);
                    tmpFile.delete();

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }

                    managerService.undeployModule(pack);

                    pack = managerService.getTemplatePackageById(DUMMY_1_MODULE);
                    assertNull(pack);
                    assertFalse("Action not unregistered", managerService.getActions().containsKey("my-post-action"));
//                    try {
//                        NodeTypeRegistry.getInstance().getNodeType("jnt:testComponent1");
//                        fail("Definition not unregistered");
//                    } catch (NoSuchNodeTypeException e) {
//                    }
                    assertTrue("Module view is not correctly unregistered", managerService.getModulesWithViewsForComponent("jnt:testComponent1").isEmpty());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    fail(e.toString());
                }

                return null;
            }
        });
    }

    @Test
    public void testModuleInstall() throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {
                    File tmpFile = File.createTempFile("module",".jar");
                    InputStream stream = managerService.getTemplatePackageById(JAHIA_TEST_MODULE).getResource(DUMMY_1_MODULE + V_2_0_0_JAR).getInputStream();
                    FileUtils.copyInputStreamToFile(stream,  tmpFile);
                    managerService.deployModule(tmpFile, session);
                    tmpFile.delete();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    fail(e.toString());
                }

                JahiaTemplatesPackage pack = managerService.getTemplatePackageById(DUMMY_1_MODULE);

                JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
                JahiaSite site = service.getSiteByKey(TESTSITE_NAME, session);

                assertFalse("Module was installed before test", site.getInstalledModules().contains(DUMMY_1_MODULE));

                managerService.installModule(pack, site.getJCRLocalPath(), session);
                session.save();
                
                assertTrue("Module has not been installed on site", site.getInstalledModules().contains(DUMMY_1_MODULE));
                assertTrue("Module content has not been copied", session.itemExists("/sites/"+TESTSITE_NAME+"/contents/test-contents"));

                managerService.uninstallModule(pack, site.getJCRLocalPath(), session);
                session.save();
                assertFalse("Module has not been removed from site", site.getInstalledModules().contains(DUMMY_1_MODULE));
                return null;
            }
        });
    }
}
