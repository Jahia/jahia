/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.osgi;

import org.jahia.services.content.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * A test case to verify that OSGi configurations are available in JSPs using some taglib functions
 */
public class ConfigJspTest extends JahiaTestCase {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigJspTest.class);

    private final static String FIRST_TESTSITE_NAME = "configJspTest";
    private final static String FIRST_SITECONTENT_ROOT_NODE = "/sites/" + FIRST_TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        TestHelper.createSite(FIRST_TESTSITE_NAME, "localhost", TestHelper.WEB_TEMPLATES,
                                "prepackagedSites/acme.zip", "ACME.zip");
                        session.save();
                    } catch (Exception ex) {
                        logger.warn("Exception during site creation", ex);
                        fail("Exception during site creation");
                    }
                    return null;
                }
            });
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            Assert.fail();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(FIRST_TESTSITE_NAME);
    }

    @Test
    public void testJspConfig() throws RepositoryException, RenderException {
        RenderService renderService = RenderService.getInstance();
        RenderContext context = new RenderContext(getRequest(), getResponse(), getUser());
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper homeNode = session.getNode(FIRST_SITECONTENT_ROOT_NODE + "/home");
        Resource resource = new Resource(homeNode, "html", "config", Resource.CONFIGURATION_MODULE);
        context.setMainResource(resource);
        context.setSite(homeNode.getResolveSite());
        String output = renderService.render(resource, context);
        assertNotNull("Output should not be null", output);
        assertTrue("JSP output should contain configuration value for key configKey1", output.contains("configKey1=configValue1"));
        assertTrue("JSP output should contain configuration values for key configKey1", output.contains("configValues.configKey1=configValue1"));
        assertTrue("JSP output should contain configuration values for key configKey2", output.contains("configValues.configKey2=configValue2"));
        assertTrue("JSP output should contain default factory configuration values for key configKey1", output.contains("defaultFactoryConfigs.configKey1=configValue1"));
        assertTrue("JSP output should contain default factory configuration values for key configKey2", output.contains("defaultFactoryConfigs.configKey2=configValue2"));
        assertTrue("JSP output should contain test module config factory identifiers", output.contains("testModuleFactoryIdentifiers=[default, id1, id2]"));
        assertTrue("JSP output should contain test module complex configuration", output.contains("spec.template.spec.initContainers[0].command[2]=until nslookup my-db; do echo waiting for my-db; sleep 2; done;"));
    }

}
