/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.test.services.content.impl.vfs;

import org.apache.commons.io.FileUtils;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.JahiaAdminUser;
import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.helper.ContentHubHelper;
import org.jahia.ajax.gwt.helper.NavigationHelper;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.*;
import javax.jcr.nodetype.NodeType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

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
    private static final String MOUNTS_DYNAMIC_MOUNT_POINT = "/mounts/dynamic-mount-point";
    private static final String MOUNTS_DYNAMIC_MOUNT_POINT_NAME = "dynamic-mount-point";

    private static final String SIMPLE_WEAKREFERENCE_PROPERTY_NAME = "test:simpleNode";
    private static final String MULTIPLE_WEAKREFERENCE_PROPERTY_NAME = "test:multipleNode";
    private static final String MULTIPLE_I18N_WEAKREFERENCE_PROPERTY_NAME = "test:multipleI18NNode";
    private static final String TEST_EXTERNAL_WEAKREFERENCE_NODE_TYPE = "test:externalWeakReference";

    private static final String DELETION_MESSAGE = "Deleted in unit test";

    private static JahiaSite site;

    JCRSessionWrapper englishEditSession;
    JCRSessionWrapper frenchEditSession;

    private void getCleanSession() throws Exception {
        String defaultLanguage = site.getDefaultLanguage();
        JCRSessionFactory sessionFactory = JCRSessionFactory.getInstance();
        sessionFactory.closeAllSessions();
        englishEditSession = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH,
                LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
        frenchEditSession = sessionFactory.getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH,
                LanguageCodeConverters.languageCodeToLocale(defaultLanguage));
    }

    @BeforeClass
    public static void oneTimeSetUp()
            throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME);

        File sysTempDir = new File(System.getProperty("java.io.tmpdir"));

        staticMountDir = new File(sysTempDir, STATIC_MOUNT_FILE_NAME);
        if (!staticMountDir.exists()) {
            staticMountDir.mkdir();
        }

        dynamicMountDir = new File(sysTempDir, DYNAMIC_MOUNT_FILE_NAME);
        if (!dynamicMountDir.exists()) {
            dynamicMountDir.mkdir();
        }
        JahiaUser jahiaRootUser = JahiaAdminUser.getAdminUser(null);
        unMountDynamicMountPoint(jahiaRootUser);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JahiaUser jahiaRootUser = JahiaAdminUser.getAdminUser(null);
        unMountDynamicMountPoint(jahiaRootUser);
        TestHelper.deleteSite(TESTSITE_NAME);
        try {
            FileUtils.deleteDirectory(dynamicMountDir);
            FileUtils.deleteDirectory(staticMountDir);
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
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
        nodeTypes.add("jnt:mounts");
        List<String> fields = new ArrayList<String>();
        fields.add("providerKey");
        fields.add("icon");
        fields.add("name");
        fields.add("locked");
        fields.add("size");
        fields.add("jcr:lastModified");
        for (GWTJahiaNode rootNode : rootNodes) {
            assertGWTJahiaNode(rootNode, "/mounts");
            List<GWTJahiaNode> childNodes = navigationHelper.ls(rootNode.getPath(), nodeTypes, new ArrayList<String>(), new ArrayList<String>(), fields, session, Locale.getDefault());
            for (GWTJahiaNode childNode : childNodes) {
                assertGWTJahiaNode(childNode, "/mounts/" + childNode.getName());
                List<GWTJahiaNode> childChildNodes = navigationHelper.ls(childNode.getPath(), nodeTypes, new ArrayList<String>(), new ArrayList<String>(), fields, session, Locale.getDefault());
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
    public void testDynamicMount() throws Exception, GWTJahiaServiceException, RepositoryException {
        ContentHubHelper contentHubHelper = (ContentHubHelper) SpringContextSingleton.getInstance().getContext().getBean("ContentHubHelper");
        JahiaUser jahiaRootUser = JahiaAdminUser.getAdminUser(null);
        GWTJahiaNodeProperty p = new GWTJahiaNodeProperty("j:rootPath","file://" + dynamicMountDir.getAbsolutePath());
        boolean mountNodeStillExists = true;
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            contentHubHelper.mount(MOUNTS_DYNAMIC_MOUNT_POINT_NAME, "jnt:vfsMountPoint", Arrays.asList(p),session , Locale.getDefault());
            assertRootNavigation(session);

            JCRNodeWrapper mountNode = getNode(session, MOUNTS_DYNAMIC_MOUNT_POINT);
            assertNode(mountNode, 0);
            createFolder(session, "folder1", mountNode);
            JCRNodeWrapper folder1Node = getNode(session, MOUNTS_DYNAMIC_MOUNT_POINT + "/folder1");
            assertNode(folder1Node, 0);
            
            assertTrue("Node schould exist using session.nodeExists", session.nodeExists(MOUNTS_DYNAMIC_MOUNT_POINT + "/folder1"));
            assertTrue("Node schould exist using node.hasNode(simpleName)", session.getNode(MOUNTS_DYNAMIC_MOUNT_POINT).hasNode("folder1"));
            assertTrue("Node schould exist using parent.hasNode(relativePath)", session.getNode(mountNode.getParent().getPath()).hasNode(mountNode.getName() + "/folder1"));
            
            session.checkout(folder1Node);
            folder1Node.remove();
            session.save();
            
            // get mount point node from parent
            JCRNodeWrapper sameMountNode = mountNode.getParent().getNode(mountNode.getName());
            assertEquals("Node objects are not the same", mountNode, sameMountNode);
            assertEquals("Node wrappers/decorators are not of the same class", mountNode.getClass(),
                    sameMountNode.getClass());
            assertEquals("Real nodes are not the same", mountNode.getRealNode(), sameMountNode.getRealNode());

            unMountDynamicMountPoint(jahiaRootUser);

            // we must recycle session because of internal session caches.
            getCleanSession();

            session = JCRSessionFactory.getInstance().getCurrentUserSession();

            mountNodeStillExists = false;
            try {
                mountNode = session.getNode(MOUNTS_DYNAMIC_MOUNT_POINT);
                mountNodeStillExists = true;
            } catch (PathNotFoundException pnfe) {
            }
            assertFalse("Dynamic mount node should have been removed but is still present in repository !", mountNodeStillExists);
        } finally {
            if (mountNodeStillExists) {
                unMountDynamicMountPoint(jahiaRootUser);
            }
        }
    }

    private static void unMountDynamicMountPoint(JahiaUser jahiaRootUser) throws RepositoryException {
        // now let's unmount.
        JCRTemplate.getInstance().doExecuteWithSystemSession(jahiaRootUser.getName(), new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper mountNode = null;
                try {
                    mountNode = session.getNode(MOUNTS_DYNAMIC_MOUNT_POINT);
                } catch (PathNotFoundException pnfe) {
                }
                if (mountNode != null) {
                    ContentHubHelper contentHubHelper = (ContentHubHelper) SpringContextSingleton.getInstance().getContext()
                            .getBean("ContentHubHelper");

                    try {
                        contentHubHelper.unmount(mountNode.getPath(), session, null);
                    } catch (GWTJahiaServiceException e) {
                        fail(e.getMessage());
                    }
                }
                return null;
            }
        });
    }

    @Test
    public void testReferencing() throws Exception, RepositoryException, UnsupportedEncodingException {
        ContentHubHelper contentHubHelper = (ContentHubHelper) SpringContextSingleton.getInstance().getContext()
                .getBean("ContentHubHelper");
        JahiaUser jahiaRootUser = JahiaAdminUser.getAdminUser(null);
        try {

            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            GWTJahiaNodeProperty p = new GWTJahiaNodeProperty("j:rootPath","file://" + dynamicMountDir.getAbsolutePath());
            contentHubHelper.mount(MOUNTS_DYNAMIC_MOUNT_POINT_NAME, "jnt:vfsMountPoint", Arrays.asList(p),session , Locale.getDefault());

            JCRNodeWrapper mountNode = getNode(session, MOUNTS_DYNAMIC_MOUNT_POINT);
            assertNode(mountNode, 0);

            String value = "This is a test";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name1 = "test1_" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper vfsTestFile1 = mountNode.uploadFile(name1, is, mimeType);

            is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name2 = "test2_" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper vfsTestFile2 = mountNode.uploadFile(name2, is, mimeType);

            session.save();

            JCRSiteNode siteNode = (JCRSiteNode) session.getNode(SITECONTENT_ROOT_NODE);

            // simple external referencing testing, with no language specified...

            JCRNodeWrapper fileReferenceNode = siteNode.addNode("externalReferenceNode", "jnt:fileReference");
            fileReferenceNode.setProperty(Constants.NODE, vfsTestFile1);
            session.save();

            Property externalReferenceProperty = fileReferenceNode.getProperty(Constants.NODE);
            Node externalNode = externalReferenceProperty.getNode();
            assertEquals("External node identifier retrieved from reference do not match", vfsTestFile1.getIdentifier(),
                    externalNode.getIdentifier());
            PropertyIterator weakReferenceProperties = vfsTestFile1.getWeakReferences(Constants.NODE);
            boolean foundWeakReferenceProperty = false;
            while (weakReferenceProperties.hasNext()) {
                Property property = weakReferenceProperties.nextProperty();
                if (property.getName().equals(Constants.NODE) && property.getParent().getIdentifier().equals(fileReferenceNode.getIdentifier())) {
                    foundWeakReferenceProperty = true;
                    break;
                }
            }
            assertTrue("Expected to find weak reference property j:node but it wasn't found !", foundWeakReferenceProperty);
            assertTrue("Expected to find j:node property when testing for it's presence but it wasn't found.",
                    fileReferenceNode.hasProperty(Constants.NODE));

            // Now let's test accessing using property iterator

            boolean foundReferenceProperty = false;
            PropertyIterator fileReferenceProperties = fileReferenceNode.getProperties();
            while (fileReferenceProperties.hasNext()) {
                Property property = fileReferenceProperties.nextProperty();
                if (property.getName().equals(Constants.NODE)) {
                    foundReferenceProperty = true;
                    break;
                }
            }
            assertTrue("Couldn't find property j:node using property iterators", foundReferenceProperty);

            fileReferenceProperties = fileReferenceNode.getProperties("j:nod* | j:*ode");
            while (fileReferenceProperties.hasNext()) {
                Property property = fileReferenceProperties.nextProperty();
                if (property.getName().equals(Constants.NODE)) {
                    foundReferenceProperty = true;
                    break;
                }
            }
            assertTrue("Couldn't find property j:node using property iterators and name patterns", foundReferenceProperty);

            // as our own property iterators also support the Map interface, we will test that now.
            Map fileReferencePropertiesMap = (Map) fileReferenceNode.getProperties("j:nod* | j:*ode");
            assertTrue("Properties used as a map do not have the reference property j:node",
                    fileReferencePropertiesMap.containsKey(Constants.NODE));
            Value refValue = (Value) fileReferencePropertiesMap.get(Constants.NODE);
            assertTrue("Reference property could not be found in properties used as a map", refValue != null);
            assertEquals("Reference property retrieved from properties used as a map does not contain proper reference",
                    vfsTestFile1.getIdentifier(), refValue.getString());

            // TODO add tests where we mix internal references AND external references in the same multi-valued property in different
            // languages.
            getCleanSession();
            siteNode = (JCRSiteNode) englishEditSession.getNode(SITECONTENT_ROOT_NODE);
            vfsTestFile1 = englishEditSession.getNode(MOUNTS_DYNAMIC_MOUNT_POINT + "/" + name1);
            vfsTestFile2 = englishEditSession.getNode(MOUNTS_DYNAMIC_MOUNT_POINT + "/" + name2);

            JCRNodeWrapper mixedFileReferenceNode = siteNode.addNode("externalMixedReferenceNode", TEST_EXTERNAL_WEAKREFERENCE_NODE_TYPE);
            mixedFileReferenceNode.setProperty(SIMPLE_WEAKREFERENCE_PROPERTY_NAME, vfsTestFile1);
            ValueFactory valueFactory = englishEditSession.getValueFactory();

            List<Value> values = new ArrayList<Value>();
            values.add(session.getValueFactory().createValue(vfsTestFile2,true));

            is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            JCRNodeWrapper siteFile1 = siteNode.uploadFile(name1, is, mimeType);
            values.add(valueFactory.createValue(siteFile1));

            Value[] multipleWeakRefs = values.toArray(new Value[values.size()]);

            mixedFileReferenceNode.setProperty(MULTIPLE_WEAKREFERENCE_PROPERTY_NAME, multipleWeakRefs);
            englishEditSession.save();

            // let's get another session to make sure we don't have cache issues
            getCleanSession();

            mixedFileReferenceNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE + "/externalMixedReferenceNode");

            assertTrue("Couldn't find property when testing for it's presence with the hasProperty method",
                    mixedFileReferenceNode.hasProperty(SIMPLE_WEAKREFERENCE_PROPERTY_NAME));
            Property simpleRefProperty = mixedFileReferenceNode.getProperty(SIMPLE_WEAKREFERENCE_PROPERTY_NAME);
            assertTrue("Reference property does not have proper value",
                    simpleRefProperty.getNode().getIdentifier().equals(vfsTestFile1.getIdentifier()));

            Property multipleRefProperty = mixedFileReferenceNode.getProperty(MULTIPLE_WEAKREFERENCE_PROPERTY_NAME);
            assertTrue("Expected multiple property but it is not multi-valued", multipleRefProperty.isMultiple());
            Value[] multipleRefPropertyValues = multipleRefProperty.getValues();
            assertTrue("First property value type is not correct", multipleRefPropertyValues[0].getType() == PropertyType.WEAKREFERENCE);
            assertTrue("First property value does not match VFS test file 2",
                    multipleRefPropertyValues[0].getString().equals(vfsTestFile2.getIdentifier()));
            assertTrue("Second property value type is not correct", multipleRefPropertyValues[1].getType() == PropertyType.WEAKREFERENCE);
            assertTrue("Second property value does not match site test file 1",
                    multipleRefPropertyValues[1].getString().equals(siteFile1.getIdentifier()));

            // TODO we will have to set the last property in multiple languages. We will need multiple
            // session objects for this.

            // TODO add tests for reference removal, making sure we don't have dangling references.

            // TODO add tests for handling missing reference targets.
        } finally {
            unMountDynamicMountPoint(jahiaRootUser);
        }
    }

    @Test
    public void testMarkForDeletion() throws Exception, RepositoryException, UnsupportedEncodingException {
        ContentHubHelper contentHubHelper = (ContentHubHelper) SpringContextSingleton.getInstance().getContext().getBean("ContentHubHelper");
        JahiaUser jahiaRootUser = JahiaAdminUser.getAdminUser(null);
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            GWTJahiaNodeProperty p = new GWTJahiaNodeProperty("j:rootPath","file://" + dynamicMountDir.getAbsolutePath());
            contentHubHelper.mount(MOUNTS_DYNAMIC_MOUNT_POINT_NAME, "jnt:vfsMountPoint", Arrays.asList(p),session , Locale.getDefault());

            JCRNodeWrapper mountNode = getNode(session, MOUNTS_DYNAMIC_MOUNT_POINT);
            assertNode(mountNode, 0);

            String value = "This is a test";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name1 = "test1_" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper vfsTestFile1 = mountNode.uploadFile(name1, is, mimeType);

            is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name2 = "test2_" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper vfsTestFile2 = mountNode.uploadFile(name2, is, mimeType);

            session.save();

            getCleanSession();

            JCRSiteNode siteNode = (JCRSiteNode) englishEditSession.getNode(SITECONTENT_ROOT_NODE);
            vfsTestFile1 = getNode(englishEditSession, MOUNTS_DYNAMIC_MOUNT_POINT + "/" + name1);
            assertFalse("Node should not allow mark for deletion", vfsTestFile1.canMarkForDeletion());

            boolean unsupportedRepositoryOperation = false;
            try {
                vfsTestFile1.markForDeletion(DELETION_MESSAGE);
            } catch (UnsupportedRepositoryOperationException uroe) {
                unsupportedRepositoryOperation = true;
            }
            assertTrue("Mark for deletion should not be allowed", unsupportedRepositoryOperation);
            englishEditSession.save();

            assertFalse("jmix:markedForDeletionRoot set", vfsTestFile1.isNodeType(Constants.JAHIAMIX_MARKED_FOR_DELETION_ROOT));
            assertFalse("jmix:markedForDeletion set", vfsTestFile1.isNodeType(Constants.JAHIAMIX_MARKED_FOR_DELETION));
            assertFalse("marked for deletion comment not set",
                    DELETION_MESSAGE.equals(vfsTestFile1.getPropertyAsString(Constants.MARKED_FOR_DELETION_MESSAGE)));
            assertFalse("j:deletionUser not set", vfsTestFile1.hasProperty(Constants.MARKED_FOR_DELETION_USER));
            assertFalse("j:deletionDate not set", vfsTestFile1.hasProperty(Constants.MARKED_FOR_DELETION_DATE));

            unsupportedRepositoryOperation = false;
            try {
                vfsTestFile1.unmarkForDeletion();
            } catch (UnsupportedRepositoryOperationException uroe) {
                unsupportedRepositoryOperation = true;
            }
            assertTrue("Unmark for deletion should not be allowed", unsupportedRepositoryOperation);
        } finally {
            unMountDynamicMountPoint(jahiaRootUser);
        }
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
