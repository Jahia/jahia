/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.templates;

import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test cases for the {@link JahiaTemplateManagerService}.
 * 
 * @author rincevent
 */
public class JahiaTemplateManagerServiceTest {
    private static Logger logger = LoggerFactory.getLogger(JahiaTemplateManagerServiceTest.class);
    private final static String TEST_SITE_NAME = "templateManagerServiceTest";
    private final static String SITE_CONTENT_ROOT_NODE = "/sites/" + TEST_SITE_NAME;

    private static List<String> excludedNodeName = Arrays.asList("j:versionInfo", "templates","permissions");

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = null;
        try {
            site = TestHelper.createSite(TEST_SITE_NAME, "localhost" + System.currentTimeMillis(), TestHelper.INTRANET_TEMPLATES);
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
            fail();
        }

        JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRUserNode user = userManager.createUser("user1", "password", new Properties(), session);

        session.save();

        JahiaGroupManagerService groupManager = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        assertNotNull("JahiaGroupManagerService cannot be retrieved", groupManager);
        if (site != null) {
            JCRGroupNode group = groupManager.lookupGroup(site.getSiteKey(), "site-privileged");
            group.addMember(user);
            group.getSession().save();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {

        try {
            TestHelper.deleteSite(TEST_SITE_NAME);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testDeploySimpleModule() throws RepositoryException {
        String moduleToBeDeployed = "article";
        deployModule(moduleToBeDeployed);
    }

    @Test
    public void testDeployComplexModule() throws RepositoryException {
        String moduleToBeDeployed = "forum";
        deployModule(moduleToBeDeployed);
    }

    private void deployModule(String moduleToBeDeployed) throws RepositoryException {
        JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        List<JahiaTemplatesPackage> availableTemplatePackages = templateManagerService.getAvailableTemplatePackages();
        assertNotNull(availableTemplatePackages);
        assertFalse(availableTemplatePackages.isEmpty());
        JahiaTemplatesPackage articlePackage = null;
        for (JahiaTemplatesPackage availableTemplatePackage : availableTemplatePackages) {
            if (availableTemplatePackage.getId().equals(moduleToBeDeployed)) {
                articlePackage = availableTemplatePackage;
            }
        }
        assertNotNull(articlePackage);
        String modulePath = "/modules/" + articlePackage.getIdWithVersion();
        templateManagerService.installModule(articlePackage.getId(), SITE_CONTENT_ROOT_NODE, "root");
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        JCRSessionWrapper session = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRSiteNode siteNode = (JCRSiteNode) session.getNode(SITE_CONTENT_ROOT_NODE);
        assertTrue(siteNode.getInstalledModules().contains(moduleToBeDeployed));
        JCRNodeWrapper node = session.getNode(modulePath);
        assertModuleIsInstalledInSite(node, modulePath, SITE_CONTENT_ROOT_NODE, session);
    }

    private void assertModuleIsInstalledInSite(JCRNodeWrapper node, String oldPrefix, String newPrefix, JCRSessionWrapper session) {
        if (!excludedNodeName.contains(node.getName())) {
            try {
                // StudioOnly node are not deployed on sites
                if(!node.isNodeType("jmix:studioOnly") && !node.isNodeType("jnt:templatesFolder")) {
                    session.getNode(node.getPath().replaceAll(oldPrefix, newPrefix));
                    NodeIterator nodeIterator = node.getNodes();
                    while (nodeIterator.hasNext()) {
                        assertModuleIsInstalledInSite((JCRNodeWrapper) nodeIterator.nextNode(), oldPrefix, newPrefix, session);
                    }
                }
            } catch (RepositoryException e) {
                fail("Error while getting node " + node.getPath().replaceAll(oldPrefix, newPrefix));
            }
        }
    }

}
