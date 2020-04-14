/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
