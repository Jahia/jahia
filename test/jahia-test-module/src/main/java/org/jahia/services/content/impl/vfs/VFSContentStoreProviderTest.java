/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content.impl.vfs;

import org.apache.commons.io.FileUtils;
import org.jahia.api.Constants;
import org.jahia.test.JahiaAdminUser;
import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.helper.ContentHubHelper;
import org.jahia.ajax.gwt.helper.NavigationHelper;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.*;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Unit test for VFS content store provider.
 * 
 * @author loom
 * Date: Aug 20, 2010
 * Time: 3:29:57 PM
 */
public class VFSContentStoreProviderTest {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(VFSContentStoreProviderTest.class);
    private static final String TESTSITE_NAME = "vfsContentProviderTest";
    private static final String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private static File dynamicMountDir;
    private static File staticMountDir;
    private static final String STATIC_MOUNT_FILE_NAME = "staticMountDirectory";
    private static final String DYNAMIC_MOUNT_FILE_NAME = "dynamicMountDirectory";
    private static final String MOUNTS_STATIC_MOUNT_POINT = "/mounts/static-mount-point";
    private static final String MOUNTS_DYNAMIC_MOUNT_POINT = "/mounts/dynamic-mount-point";
    private static final String MOUNTS_DYNAMIC_MOUNT_POINT_NAME = "dynamic-mount-point";

    @BeforeClass
    public static void oneTimeSetUp()
            throws Exception {
        TestHelper.createSite(TESTSITE_NAME, "localhost", "templates-web");
        
        File sysTempDir = new File(System.getProperty("java.io.tmpdir"));

        staticMountDir = new File(sysTempDir, STATIC_MOUNT_FILE_NAME);
        if (!staticMountDir.exists()) {
            staticMountDir.mkdir();
        }

        dynamicMountDir = new File(sysTempDir, DYNAMIC_MOUNT_FILE_NAME);
        if (!dynamicMountDir.exists()) {
            dynamicMountDir.mkdir();
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
        try {
            FileUtils.deleteDirectory(dynamicMountDir);
            FileUtils.deleteDirectory(staticMountDir);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    @Test
    public void testStaticMount() throws JahiaInitializationException, RepositoryException, GWTJahiaServiceException {
        VFSContentStoreProvider vfsProvider = new VFSContentStoreProvider();
        vfsProvider.setKey("local");
        vfsProvider.setRoot("file://" + staticMountDir.getAbsolutePath());
        //vfsProvider.setRmibind("local");
        vfsProvider.setMountPoint(MOUNTS_STATIC_MOUNT_POINT);
        vfsProvider.setUserManagerService(ServicesRegistry.getInstance().getJahiaUserManagerService());
        vfsProvider.setGroupManagerService(ServicesRegistry.getInstance().getJahiaGroupManagerService());
        vfsProvider.setSitesService(ServicesRegistry.getInstance().getJahiaSitesService());
        vfsProvider.setService(ServicesRegistry.getInstance().getJCRStoreService());
        JCRSessionFactory jcrSessionFactory = (JCRSessionFactory) SpringContextSingleton.getInstance().getContext().getBean("jcrSessionFactory");
        vfsProvider.setSessionFactory(jcrSessionFactory);
        JCRPublicationService jcrPublicationService = (JCRPublicationService) SpringContextSingleton.getInstance().getContext().getBean("jcrPublicationService");
        vfsProvider.setPublicationService(jcrPublicationService);
        vfsProvider.start();

        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            assertRootNavigation(session);
    
            JCRNodeWrapper mountNode = getNode(session, MOUNTS_STATIC_MOUNT_POINT);
            assertNode(mountNode, 0);
            createFolder(session, "folder1", mountNode);
            JCRNodeWrapper folder1Node = getNode(session, MOUNTS_STATIC_MOUNT_POINT + "/folder1");
            assertNode(folder1Node, 0);
            session.checkout(folder1Node);
            folder1Node.remove();
            session.save();
        } finally {
            vfsProvider.stop();
        }
    }

    private void assertRootNavigation(JCRSessionWrapper session) throws RepositoryException, GWTJahiaServiceException {
        JCRSiteNode siteNode = (JCRSiteNode) session.getNode(SITECONTENT_ROOT_NODE);
        NavigationHelper navigationHelper = (NavigationHelper) SpringContextSingleton.getInstance().getContext().getBean("NavigationHelper");
        Locale locale = LanguageCodeConverters.languageCodeToLocale("en");
        List<String> paths = new ArrayList<String>();
        paths.add("/mounts");
        List<GWTJahiaNode> rootNodes = navigationHelper.retrieveRoot(paths, null,null,null,null,
                            null,null,siteNode, session, locale);
        List<String> nodeTypes = new ArrayList<String>();
        nodeTypes.add("nt:file");
        nodeTypes.add("nt:folder");
        nodeTypes.add("jnt:mountPoints");
        List<String> fields = new ArrayList<String>();
        fields.add("providerKey");
        fields.add("icon");
        fields.add("name");
        fields.add("locked");
        fields.add("size");
        fields.add("jcr:lastModified");
        for (GWTJahiaNode rootNode : rootNodes) {
            assertGWTJahiaNode(rootNode, "/mounts");
            List<GWTJahiaNode> childNodes = navigationHelper.ls(rootNode, nodeTypes, new ArrayList<String>(), new ArrayList<String>(), fields, session);
            for (GWTJahiaNode childNode : childNodes) {
                assertGWTJahiaNode(childNode, "/mounts/" + childNode.getName());
                List<GWTJahiaNode> childChildNodes = navigationHelper.ls(childNode, nodeTypes, new ArrayList<String>(), new ArrayList<String>(), fields, session);                
            }
        }
    }

    private void assertGWTJahiaNode(GWTJahiaNode jahiaGWTNode, String expectedPath) {
        assertEquals("Expected path and actual GWT node path are not equal !", expectedPath, jahiaGWTNode.getPath());
        int lastSlashPosInPath = jahiaGWTNode.getPath().lastIndexOf("/");
        if (lastSlashPosInPath > -1)
            assertEquals("Last part of path and name are not equal !", jahiaGWTNode.getPath().substring(lastSlashPosInPath + 1), jahiaGWTNode.getName());
        else {
            assertEquals("Last part of path and name are not equal !", jahiaGWTNode.getPath(), jahiaGWTNode.getName());
        }
    }

    @Test
    public void testDynamicMount() throws GWTJahiaServiceException, RepositoryException {
        ContentHubHelper contentHubHelper = (ContentHubHelper) SpringContextSingleton.getInstance().getContext().getBean("ContentHubHelper");
        JahiaUser jahiaRootUser = JahiaAdminUser.getAdminUser(0);
        contentHubHelper.mount(MOUNTS_DYNAMIC_MOUNT_POINT_NAME, "file://" + dynamicMountDir.getAbsolutePath(), jahiaRootUser);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        assertRootNavigation(session);

        JCRNodeWrapper mountNode = getNode(session, MOUNTS_DYNAMIC_MOUNT_POINT);
        assertNode(mountNode, 0);
        createFolder(session, "folder1", mountNode);
        JCRNodeWrapper folder1Node = getNode(session, MOUNTS_DYNAMIC_MOUNT_POINT + "/folder1");
        assertNode(folder1Node, 0);
        session.checkout(folder1Node);
        folder1Node.remove();
        session.save();

        mountNode.remove();
        session.save();

    }

    @Test
    public void testReferencing() throws GWTJahiaServiceException, RepositoryException, UnsupportedEncodingException {
        ContentHubHelper contentHubHelper = (ContentHubHelper) SpringContextSingleton.getInstance().getContext().getBean("ContentHubHelper");
        JahiaUser jahiaRootUser = JahiaAdminUser.getAdminUser(0);
        contentHubHelper.mount(MOUNTS_DYNAMIC_MOUNT_POINT_NAME, "file://" + dynamicMountDir.getAbsolutePath(), jahiaRootUser);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        JCRNodeWrapper mountNode = getNode(session, MOUNTS_DYNAMIC_MOUNT_POINT);
        assertNode(mountNode, 0);

        String value = "This is a test";
        String mimeType = "text/plain";

        InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

        String name1 = "test1_" + System.currentTimeMillis() + ".txt";
        JCRNodeWrapper testFile1 = mountNode.uploadFile(name1, is, mimeType);

        session.save();

        JCRSiteNode siteNode = (JCRSiteNode) session.getNode(SITECONTENT_ROOT_NODE);

        // simple external referencing testing...

        JCRNodeWrapper fileReferenceNode = siteNode.addNode("externalReferenceNode", "jnt:fileReference");
        fileReferenceNode.setProperty("j:node", testFile1);
        session.save();

        Property externalReferenceProperty = fileReferenceNode.getProperty("j:node");
        Node externalNode = externalReferenceProperty.getNode();
        assertEquals("External node identifier retrieved from reference do not match", testFile1.getIdentifier(), externalNode.getIdentifier());

        // TODO add tests where we use property iterators to retrieve external reference properties, as this is currently not implemented

        // TODO add tests where we mix internal references AND external references in the same property in different languages.

        mountNode.remove();
        session.save();

    }

    private JCRNodeWrapper getNode(JCRSessionWrapper session, String path) throws RepositoryException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            return node;
        } catch (PathNotFoundException pnfe) {
            logger.error("Mount point not available", pnfe);
            assertTrue("Node at " + path + " could not be found !", false);
        }
        return null;
    }

    private void assertNode(Node node, int depth)
            throws RepositoryException {
        NodeType primaryNodeType = node.getPrimaryNodeType();
        assertNotNull("Primary node type is null !", primaryNodeType);
        NodeDefinition nodeDefinition = node.getDefinition();
        assertNotNull("Node definition is null !", nodeDefinition);
        String nodeIdentifier = node.getIdentifier();
        assertNotNull("Node identifier is null!", nodeIdentifier);
        NodeType[] nodeMixinNodeTypes = node.getMixinNodeTypes();
        assertNotNull("Mixin types are null !", nodeMixinNodeTypes);
        assertNotNull("Node path is null!", node.getPath());
        assertNotNull("Node name is null!", node.getName());
        int lastSlashPosInPath = node.getPath().lastIndexOf("/");
        if (lastSlashPosInPath > -1)
            assertEquals("Last part of path and name are not equal !", node.getPath().substring(lastSlashPosInPath + 1), node.getName());
        else {
            assertEquals("Last part of path and name are not equal !", node.getPath(), node.getName());
        }

        PropertyIterator propertyIterator = node.getProperties();
        assertNotNull("Property iterator is null !", propertyIterator);
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();
            property.isMultiple();
            property.isNew();
            property.isNode();
        }
        NodeIterator nodeIterator = node.getNodes();
        assertNotNull("Child node iterator is null !", nodeIterator);
        while (nodeIterator.hasNext()) {
            Node childNode = nodeIterator.nextNode();
            assertNode(childNode, depth + 1);
        }
    }

    private void createFolder(JCRSessionWrapper session, String name, Node node) throws RepositoryException {
        session.checkout(node);
        node.addNode(name, "jnt:folder");
        session.save();
    }

}
