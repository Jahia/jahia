/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.test.osgi;

import org.apache.commons.io.FileUtils;
import org.jahia.bin.Action;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * SpringContextSingleton is using wait/notify mechanism when a bean is not found in module contexts
 * This tests are made to test this mechanism. Asking beans, then start/stop modules that bring the asked bean or not
 * doing this operations in parallel in different threads to simulate DX startup
 */
public class SpringContextSingletonTest {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SpringContextSingletonTest.class);

    private static JahiaTemplateManagerService managerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();

    private static long originalModuleSpringBeansWaitingTimeout;
    private static int originalTagBundleState;

    private static Bundle dummy1Bundle;
    private static Bundle tagsBundle;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            installTestModule("dummy1-1.0.jar");
            dummy1Bundle = BundleUtils.getBundle("dummy1", "1.0");

            JahiaTemplatesPackage articlePack = managerService.getTemplatePackageById("tags");
            tagsBundle = articlePack.getBundle();
            originalTagBundleState = tagsBundle.getState();

            originalModuleSpringBeansWaitingTimeout = SettingsBean.getInstance().getModuleSpringBeansWaitingTimeout();
            SettingsBean.getInstance().setModuleSpringBeansWaitingTimeout(10);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            fail();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            dummy1Bundle.uninstall();
            if (originalTagBundleState == Bundle.ACTIVE && tagsBundle.getState() != Bundle.ACTIVE) {
                tagsBundle.start();
            }

            SettingsBean.getInstance().setModuleSpringBeansWaitingTimeout(originalModuleSpringBeansWaitingTimeout);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    @Test
    public void simpleTestGetBeanInModulesContext() throws Exception {
        new StartModuleThread(dummy1Bundle, 5000).start();

        GetBeanThread g1 = new GetBeanThread("dummy1Action");
        GetBeanThread g2 = new GetBeanThread("BeanThatDoesNotExist");

        g1.start();
        g2.start();

        g1.join();
        g2.join();

        assertNull(g2.getResult());
        assertNotNull(g1.getResult());
        dummy1Bundle.stop();
    }

    @Test
    public void testOtherModuleStartedDuringWait() throws Exception {
        tagsBundle.stop();
        new StartModuleThread(tagsBundle, 5000).start();

        GetBeanThread g1 = new GetBeanThread("dummy1Action");
        GetBeanThread g2 = new GetBeanThread("BeanThatDoesNotExist");

        g1.start();
        g2.start();

        g1.join();
        g2.join();

        assertNull(g2.getResult());
        assertNull(g1.getResult());
    }

    @Test
    public void testOtherModuleStartedDuringWait2() throws Exception {
        tagsBundle.stop();
        new StartModuleThread(tagsBundle, 3000).start();
        new StartModuleThread(dummy1Bundle, 5000).start();

        GetBeanThread g1 = new GetBeanThread("dummy1Action");
        GetBeanThread g2 = new GetBeanThread("BeanThatDoesNotExist");

        g1.start();
        g2.start();

        g1.join();
        g2.join();

        assertNull(g2.getResult());
        assertNotNull(g1.getResult());
        dummy1Bundle.stop();
    }

    @Test
    public void testMultipleWaits() throws Exception {
        tagsBundle.stop();
        new StartModuleThread(tagsBundle, 7000).start();
        new StartModuleThread(dummy1Bundle, 3000).start();

        GetBeanThread g1 = new GetBeanThread("dummy1Action");
        GetBeanThread g2 = new GetBeanThread("org.jahia.modules.tags.actions.RemoveTag#0");
        GetBeanThread g3 = new GetBeanThread("org.jahia.modules.tags.actions.MatchingTags#0");
        GetBeanThread g4 = new GetBeanThread("BeanThatDoesNotExist");

        g1.start();
        g2.start();
        g3.start();
        g4.start();

        g1.join(); // G1 is released first because only 3 sec before start dummy module, g2, g3 and g4 are still waiting at that point
        assertNotNull(g1.getResult());
        assertNull(g2.getResult());
        assertNull(g3.getResult());
        assertNull(g4.getResult());

        g2.join(); // g2 and g3 will be release when tag bundle is started
        g3.join();
        assertNotNull(g2.getResult());
        assertNotNull(g3.getResult());
        assertNull(g4.getResult()); // g4 at that moment is still waiting and will be timed out

        g4.join(); // g4 timed out
        assertNull(g4.getResult());

        dummy1Bundle.stop();
    }

    @Test
    public void testSimultaneousOperation() throws Exception {
        GetBeanThread g1 = new GetBeanThread("dummy1Action");
        new StartModuleThread(dummy1Bundle, 0).start();

        g1.start();
        g1.join();

        assertNotNull(g1.getResult());
        dummy1Bundle.stop();
    }

    @Test
    public void testUnsupportedCallStack() throws Exception {
        StartModuleThread s1 = new StartModuleThread(dummy1Bundle, 5000);
        s1.start();

        UnsupporteGetBeanThread g1 = new UnsupporteGetBeanThread("dummy1Action");
        GetBeanThread g3 = new GetBeanThread("dummy1Action");
        g1.start();
        g3.start();

        g1.join();
        assertNull(g1.getResult());
        assertFalse(s1.isModuleStarted()); // G1 will not wait since UnsupporteGetBeanThread is not allowed in the call stack

        g3.join();
        assertNotNull(g3.getResult());
        assertTrue(s1.isModuleStarted()); // G3 will wait, GetBeanThread is allowed in the call stack

        UnsupporteGetBeanThread g2 = new UnsupporteGetBeanThread("dummy1Action");
        g2.start();
        g2.join();
        assertNotNull(g2.getResult()); // G2 will not wait, but bean will be found since module is started

        dummy1Bundle.stop();
    }

    public class GetBeanThread extends Thread {
        String beanId;
        Object result = null;

        public GetBeanThread(String beanId) {
            this.beanId = beanId;
        }

        @Override
        public void run() {
            result = SpringContextSingleton.getBeanInModulesContext(beanId);
        }

        public Object getResult() {
            return result;
        }
    }

    public class UnsupporteGetBeanThread extends GetBeanThread {
        public UnsupporteGetBeanThread(String beanId) {
            super(beanId);
        }

        @Override
        public void run() {
            result = SpringContextSingleton.getBeanInModulesContext(beanId);
        }
    }

    public class StartModuleThread extends Thread {
        Bundle bundleToStart;
        long sleepTime = 0;
        boolean moduleStarted = false;

        public StartModuleThread(Bundle bundle, long sleepTime) {
            this.bundleToStart = bundle;
            this.sleepTime = sleepTime;
        }

        @Override
        public void run() {
            try {
                if (sleepTime > 0) {
                    sleep(sleepTime);
                }

                try {
                    bundleToStart.start();
                    moduleStarted = true;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    fail(e.getMessage());
                }

            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        public boolean isModuleStarted() {
            return moduleStarted;
        }
    }

    private static void installTestModule(final String moduleName) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            @Override
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                try {

                    File tmpFile = File.createTempFile("module",".jar");
                    InputStream stream = managerService.getTemplatePackageById("jahia-test-module").getResource(moduleName).getInputStream();
                    FileUtils.copyInputStreamToFile(stream,  tmpFile);
                    managerService.deployModule(tmpFile, session);
                    logger.info("Module " + moduleName + " deployed");
                    tmpFile.delete();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    fail(e.toString());
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                JahiaTemplatesPackage pack = managerService.getTemplatePackageById("dummy1");
                assertNotNull(pack);
                assertEquals("Module is not started", ModuleState.State.STARTED, pack.getState().getState());
                assertNotNull("Spring context is null", pack.getContext());
                assertTrue("No action defined", !pack.getContext().getBeansOfType(Action.class).isEmpty());
                assertTrue("Action not registered", managerService.getActions().containsKey("my-post-action"));

                try {
                    pack.getBundle().stop();
                } catch (BundleException e) {
                    logger.error(e.getMessage(), e);
                    fail(e.toString());
                }
                return null;
            }
        });
    }
}
