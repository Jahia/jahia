package org.jahia.services.content.nodetypes;

import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.test.framework.AbstractJUnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.Locale;

/**
 * Node types related tests
 */
public class NodeTypesIT extends AbstractJUnitTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(NodeTypesChangesIT.class);

    private JCRSessionWrapper session;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, Locale.ENGLISH, session -> {
            session.getNode("/").addNode("nodeTypesTest", "jnt:contentList");
            session.save();
            return null;
        });
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, Constants.EDIT_WORKSPACE, Locale.ENGLISH, session -> {
            if (session.nodeExists("/nodeTypesTest")) {
                session.getNode("/nodeTypesTest").remove();
                session.save();
            }
            return null;
        });
    }

    @Before
    public void setUp() throws RepositoryException {
        session = JCRSessionFactory.getInstance().getCurrentUserSession();
    }

    @Test
    public void mandatoryOverridePropertyShouldBeMandatory() throws Exception {
        // given
        JCRNodeWrapper rootNode = session.getNode("/");
        JCRNodeWrapper testNode = rootNode.addNode("testPropertyOverride", "test:nodeTypeOverride");

        // when
        try {
            session.save();
            Assert.fail("Missing mandatory properties [mandatoryOverrideProperty, overrideProperty], save should fail");
        } catch (CompositeConstraintViolationException e) {
            // then
            Assert.assertTrue(e.getMessage().contains("mandatoryOverrideProperty: Field is mandatory"));
            Assert.assertTrue(e.getMessage().contains("overrideProperty: Field is mandatory"));
        }

        // given
        testNode.setProperty("overrideProperty", "dummy");
        // when
        try {
            session.save();
            Assert.fail("Missing mandatory property [mandatoryOverrideProperty], save should fail");
        } catch (CompositeConstraintViolationException e) {
            // then
            Assert.assertTrue(e.getMessage().contains("mandatoryOverrideProperty: Field is mandatory"));
            Assert.assertFalse(e.getMessage().contains("overrideProperty: Field is mandatory"));
        }

        // given
        testNode.setProperty("mandatoryOverrideProperty", "dummy");
        // when
        try {
            session.save();
        } catch (Exception e) {
            // then
            Assert.fail("Mandatory properties are correctly set, save should work");
        }
    }
}
