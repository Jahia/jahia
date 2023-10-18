/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.nodetypes;

import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.Locale;

import static org.jahia.services.sites.JahiaSitesService.SYSTEM_SITE_KEY;

/**
 * Node types related tests
 */
public class NodeTypesIT extends AbstractJUnitTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(NodeTypesChangesIT.class);

    private JCRSessionWrapper session;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();

        TestHelper.createSite(SYSTEM_SITE_KEY, null);
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

    @After
    public void teardown() throws RepositoryException {
        session.refresh(false);
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

    @Test
    public void mandatoryOverridePropertyShouldBeMandatoryAndAutocreated() throws Exception {
        // given
        JCRNodeWrapper rootNode = session.getNode("/");
        JCRNodeWrapper testNode = rootNode.addNode("nodeTypeOverrideNodeTypeWithAutocreated", "test:nodeTypeOverrideNodeTypeWithAutocreated");
        JCRNodeWrapper testNodeWithMixin = rootNode.addNode("nodeTypeOverrideWithAutocreated", "test:nodeTypeOverrideWithAutocreated");

        // when
        session.save();

        // then
        Assert.assertTrue("Missing mandatory localProperty should have been autocreated", testNode.hasProperty("localProperty"));
        Assert.assertTrue("Missing mandatory mandatoryOverridePropertyNodeType should have been autocreated", testNode.hasProperty("mandatoryOverridePropertyNodeType"));
        Assert.assertTrue("Missing mandatory mandatoryOverrideProperty should have been autocreated", testNodeWithMixin.hasProperty("mandatoryOverrideProperty"));

        // Mandatory auto created property can not be removed
        testNode.getProperty("localProperty").remove();
        try {
            session.save();
            Assert.fail("localProperty property cannot be removed, property is mandatory");
        } catch (CompositeConstraintViolationException e) {
            session.refresh(false);
            Assert.assertTrue("Missing mandatory localProperty should have not be removed", session.getNode(testNode.getPath()).hasProperty("localProperty"));
        }
        testNode.getProperty("mandatoryOverridePropertyNodeType").remove();
        try {
            session.save();
            Assert.fail("mandatoryOverridePropertyNodeType property cannot be removed, property is mandatory");
        } catch (CompositeConstraintViolationException e) {
            session.refresh(false);
            Assert.assertTrue("Missing mandatory mandatoryOverridePropertyNodeType should have not be removed", session.getNode(testNode.getPath()).hasProperty("mandatoryOverridePropertyNodeType"));
        }
        testNodeWithMixin.getProperty("mandatoryOverrideProperty").remove();
        try {
            session.save();
            Assert.fail("mandatoryOverrideProperty property cannot be removed, property is mandatory");
        } catch (CompositeConstraintViolationException e) {
            session.refresh(false);
            Assert.assertTrue("Missing mandatory mandatoryOverrideProperty should have not be removed", session.getNode(testNodeWithMixin.getPath()).hasProperty("mandatoryOverrideProperty"));
        }
    }
}
