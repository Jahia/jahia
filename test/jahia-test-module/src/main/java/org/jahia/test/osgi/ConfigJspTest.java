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
