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

package org.jahia.services.templates;

import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.test.TestHelper;
import org.junit.*;
import org.slf4j.Logger;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 12/10/11
 * Time: 9:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaTemplateManagerServiceTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaTemplateManagerServiceTest.class);
    private final static String TEST_SITE_NAME = "templateManagerServiceTest";
    private final static String SITE_CONTENT_ROOT_NODE = "/sites/" + TEST_SITE_NAME;

    private static List<String> excludedNodeName = Arrays.asList("j:versionInfo");

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = null;
        try {
            site = TestHelper.createSite(TEST_SITE_NAME, "localhost" + System.currentTimeMillis(), TestHelper.INTRANET_TEMPLATES);
            assertNotNull(site);
        } catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }

        JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
        assertNotNull("JahiaUserManagerService cannot be retrieved", userManager);

        JahiaUser user = userManager.createUser("user1", "password", new Properties());

        JahiaGroupManagerService groupManager = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        assertNotNull("JahiaGroupManagerService cannot be retrieved", groupManager);
        if (site != null) {
            JahiaGroup group = groupManager.lookupGroup(site.getID(), "site-privileged");
            group.addMember(user);
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
            if (availableTemplatePackage.getFileName().equals(moduleToBeDeployed)) {
                articlePackage = availableTemplatePackage;
            }
        }
        assertNotNull(articlePackage);
        String modulePath = "/templateSets/" + articlePackage.getFileName();
        templateManagerService.deployModule(modulePath, SITE_CONTENT_ROOT_NODE, "root");
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        JCRSessionWrapper session = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRSiteNode siteNode = (JCRSiteNode) session.getNode(SITE_CONTENT_ROOT_NODE);
        assertTrue(siteNode.getInstalledModules().contains(moduleToBeDeployed));
        JCRNodeWrapper node = session.getNode(modulePath);
        assertTrue(compareNodeNamesTreeStopOnError(node, modulePath, SITE_CONTENT_ROOT_NODE, session));
    }

    @Test
    public void testRolesOnComponent() throws RepositoryException {
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        JCRSessionWrapper session = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper componentNode = session.getNode(SITE_CONTENT_ROOT_NODE + "/components/jmix:structuredContent/jnt:article");
        componentNode.revokeAllRoles();
        componentNode.setAclInheritanceBreak(true);
        session.save();
        assertFalse((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(componentNode.getPath(), "useComponent"))));
        componentNode.grantRoles("u:user1", Collections.singleton("editor-in-chief"));
        session.save();
        assertTrue((JCRTemplate.getInstance().doExecuteWithUserSession("user1", null, new CheckPermission(componentNode.getPath(), "useComponent"))));

    }

    private boolean compareNodeNamesTreeStopOnError(JCRNodeWrapper node, String oldPrefix, String newPrefix, JCRSessionWrapper session) {
        if (!excludedNodeName.contains(node.getName())) {
            try {
                // StudioOnly node are not deployed on sites
                if(!node.isNodeType("jmix:studioOnly")) {
                    session.getNode(node.getPath().replaceAll(oldPrefix, newPrefix).replaceAll("forum\\-base", "base/forum-base"));
                }
                NodeIterator nodeIterator = node.getNodes();
                while (nodeIterator.hasNext()) {
                    if (!compareNodeNamesTreeStopOnError((JCRNodeWrapper) nodeIterator.nextNode(), oldPrefix, newPrefix, session)) {
                        return false;
                    }
                }
            } catch (RepositoryException e) {
                logger.error("Error while getting node " + node.getPath().replaceAll(oldPrefix, newPrefix));
                return false;
            }
        }
        return true;
    }

    class CheckPermission implements JCRCallback<Boolean> {
        private String path;
        private String permission;

        CheckPermission(String path, String permission) {
            this.path = path;
            this.permission = permission;
        }

        public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
            try {
                return session.getNode(path).hasPermission(permission);
            } catch (PathNotFoundException e) {
                return false;
            }
        }
    }
}
