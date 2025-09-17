/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.apache.commons.io.IOUtils;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * This test unit tests all basic content operations on all connected providers. This is useful to test common functionality across the
 * Entropy and VFS connectors.
 */
public class ContentIT extends AbstractJUnitTest {
    private static final transient Logger logger = org.slf4j.LoggerFactory
            .getLogger(ContentIT.class);
    private final static String TESTSITE_NAME = "contentTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/"
            + TESTSITE_NAME;
    private static final String SIMPLE_REFERENCE_PROPERTY_NAME = "test:simpleNode";
    private static final String MULTIPLE_I18N_REFERENCE_PROPERTY_NAME = "test:multipleI18NNode";
    private static final String TEST_EXTERNAL_REFERENCE_NODE_TYPE = "test:externalReference";

    private static String providerRoot = SITECONTENT_ROOT_NODE;

    private static List<String> nodes = new ArrayList<String>();

    private JCRSessionWrapper session;

    protected java.lang.String getName() {
        return providerRoot;
    }

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        TestHelper.createSite(TESTSITE_NAME);
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        TestHelper.deleteSite(TESTSITE_NAME);
    }

    @Before
    public void setUp() throws RepositoryException {
        session = JCRSessionFactory.getInstance().getCurrentUserSession();
    }

    @After
    public void tearDown() throws Exception {
        JCRSessionFactory.getInstance().closeAllSessions();
        JCRTemplate.getInstance().doExecuteWithSystemSession(
                new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        for (String node : nodes) {
                            try {
                                session.getNodeByIdentifier(node).remove();
                            } catch (RepositoryException e) {
                                logger.error("Error when deleting nodes", e);
                            }
                        }
                        session.save();
                        return null;
                    }
                });
        nodes.clear();
    }

    /**
     * Test creation / deletion of folder
     *
     * @throws RepositoryException
     */
    @Test
    public void testCreateFolder() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession();

        JCRNodeWrapper rootNode = session.getNode(providerRoot);
        session.checkout(rootNode);
        final String name = "test" + System.currentTimeMillis();

        assertTrue("Root node should be writeable !",
                rootNode.hasPermission("jcr:addChildNodes"));

        JCRNodeWrapper testCollection = rootNode.createCollection(name);
        session.save();
        nodes.add(testCollection.getIdentifier());

        assertTrue(providerRoot + " : Created folder is not a collection",
                testCollection.isCollection());

        long creationDate = testCollection.getCreationDateAsDate().getTime();
        DateFormat dateFormat = DateFormat.getInstance();
        assertTrue(
                providerRoot
                        + " : Creation date invalid value="
                        + dateFormat.format(new Date(creationDate))
                        + " expected date in range from "
                        + dateFormat.format(new Date(
                                System.currentTimeMillis() - 10000))
                        + " to "
                        + dateFormat.format(new Date(
                                System.currentTimeMillis() + 10000)),
                creationDate < (System.currentTimeMillis() + 10000)
                        && creationDate > System.currentTimeMillis() - 10000);

        long lastModifiedDate = testCollection.getLastModifiedAsDate()
                .getTime();
        assertTrue(
                providerRoot
                        + " : Modification date invalid value="
                        + dateFormat.format(new Date(lastModifiedDate))
                        + " expected date in range from "
                        + dateFormat.format(new Date(
                                System.currentTimeMillis() - 10000))
                        + " to "
                        + dateFormat.format(new Date(
                                System.currentTimeMillis() + 10000)),
                lastModifiedDate < (System.currentTimeMillis() + 1000)
                        && lastModifiedDate > System.currentTimeMillis() - 10000);

        testCollection = session.getNode(providerRoot + "/" + name);
    }

    /**
     * Test file upload
     *
     * @throws RepositoryException
     */
    @Test
    public void testUpload() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession();

        JCRNodeWrapper rootNode = session.getNode(providerRoot);
        session.checkout(rootNode);
        assertTrue("Root node should be writeable !",
                rootNode.hasPermission("jcr:addChildNodes"));

        String value = "This is a test";
        String mimeType = "text/plain";

        InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

        String name = "test" + System.currentTimeMillis() + ".txt";
        JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
        session.save();
        nodes.add(testFile.getIdentifier());

        assertEquals(providerRoot + " : Size is not the same", value.length(),
                testFile.getFileContent().getContentLength());
        assertEquals(providerRoot + " : Mime type is not the same", mimeType,
                testFile.getFileContent().getContentType());

        Date creationDate = testFile.getCreationDateAsDate();
        assertNotNull(providerRoot + " : Creation date is null !", creationDate);
        long creationTime = creationDate.getTime();
        assertTrue(providerRoot + " : Creation date invalid",
                creationTime < (System.currentTimeMillis() + 600000)
                        && creationTime > (System.currentTimeMillis() - 600000));

        Date lastModifiedDate = testFile.getLastModifiedAsDate();
        assertNotNull(providerRoot + " : Last modified date is null !",
                lastModifiedDate);
        long lastModifiedTime = lastModifiedDate.getTime();
        assertTrue(
                providerRoot + " : Modification date invalid",
                lastModifiedTime < (System.currentTimeMillis() + 600000)
                        && lastModifiedTime > (System.currentTimeMillis() - 600000));

        testFile = session.getNode(providerRoot + "/" + name);

        is = testFile.getFileContent().downloadFile();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is, baos);
        String checkString = new String(baos.toByteArray(), "UTF-8");
        assertEquals(providerRoot + " : File content is different", value,
                checkString);
    }

    /**
     * Test simple property set
     *
     * @throws RepositoryException
     */
    @Test
    public void testSetStringProperty() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession();

        JCRNodeWrapper rootNode = session.getNode(providerRoot);
        session.checkout(rootNode);
        assertTrue("Root node should be writeable !",
                rootNode.hasPermission("jcr:addChildNodes"));

        final String name = "test" + System.currentTimeMillis();

        JCRNodeWrapper testCollection = rootNode.createCollection(name);
        session.save();
        nodes.add(testCollection.getIdentifier());

        final String value = "Title test";

        try {
            testCollection.setProperty("jcr:description", value);
            testCollection.getSession().save();
        } catch (ConstraintViolationException e) {
            return;
        }

        testCollection = session.getNode(providerRoot + "/" + name);
        try {
            String actual = testCollection.getProperty("jcr:description")
                    .getString();
            assertEquals(providerRoot
                    + " : getProperty() Property value is not the same", value,
                    actual);
        } catch (PathNotFoundException e) {
            fail(providerRoot + " : getProperty() cannot find property");
        }

        assertEquals(providerRoot
                + " : getPropertyAsString() : Property value is not the same",
                value, testCollection.getPropertyAsString("jcr:description"));
        assertEquals(
                providerRoot
                        + " : getPropertiesAsString() : Property value is not the same",
                value,
                testCollection.getPropertiesAsString().get("jcr:description"));

        boolean found = false;
        PropertyIterator pi = testCollection.getProperties();
        while (pi.hasNext()) {
            Property p = pi.nextProperty();
            if (p.getName().equals("jcr:description")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    /**
     * Test rename of a file
     *
     * @throws RepositoryException
     */
    @Test
    public void testRename() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession();

        JCRNodeWrapper rootNode = session.getNode(providerRoot);
        session.checkout(rootNode);
        assertTrue("Root node should be writeable !",
                rootNode.hasPermission("jcr:addChildNodes"));

        String value = "This is a test";
        String mimeType = "text/plain";

        InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

        String name = "test" + System.currentTimeMillis() + ".txt";
        JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
        session.save();

        // now let's sleep a little to give time for Jackrabbit to index the file's content
        // otherwise setting extracted text in parallel thread will conflict with renaming in the next lines
        Thread.sleep(5000);

        String initialTestFileIdentifier = testFile.getIdentifier();

        final String newname = "renamed" + name;
        boolean result = false;
        try {
            result = testFile.rename(newname);
            String renamedTestFileIdentifier = testFile.getIdentifier();
            if (!initialTestFileIdentifier.equals(renamedTestFileIdentifier)) {
                logger.warn("Test file identifier changed after rename, this is unfortunate but must be handled according to specification ! old value="
                        + initialTestFileIdentifier
                        + " new value="
                        + renamedTestFileIdentifier);
            }

            assertTrue(providerRoot + " : rename returned false", result);

            try {
                testFile = session.getNode(providerRoot + "/" + newname);
            } catch (RepositoryException e) {
                fail(providerRoot + " : Renamed file not found");
            }

            testFile.remove();
            session.save();
        } catch (UnsupportedRepositoryOperationException e) {

        }
    }

    /**
     * Test move of a file
     *
     * @throws RepositoryException
     */
    @Test
    public void testMove() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession();

        JCRNodeWrapper rootNode = session.getNode(providerRoot);
        session.checkout(rootNode);
        String value = "This is a test";
        String mimeType = "text/plain";

        InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

        String name = "test" + System.currentTimeMillis() + ".txt";
        JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
        String initialTestFileIdentifier = testFile.getIdentifier();
        // nodes.add(testFile.getIdentifier());

        final String collectionName = "foldertest" + System.currentTimeMillis();
        JCRNodeWrapper testCollection = rootNode
                .createCollection(collectionName);
        // nodes.add(testCollection.getIdentifier());

        session.save();

        try {
            session.move(testFile.getPath(), providerRoot + "/"
                    + collectionName + "/" + testFile.getName());
        } catch (RepositoryException e) {
            fail(providerRoot + " : move throwed exception :" + e);
        }

        session.save();

        try {
            session.getNode(providerRoot + "/" + collectionName + "/"
                    + testFile.getName());
        } catch (RepositoryException e) {
            fail(providerRoot + " : moved file not found");
        }

        String renamedTestFileIdentifier = testFile.getIdentifier();
        if (!initialTestFileIdentifier.equals(renamedTestFileIdentifier)) {
            logger.warn("Test file identifier changed after rename, this is unfortunate but must be handled according to specification ! old value="
                    + initialTestFileIdentifier
                    + " new value="
                    + renamedTestFileIdentifier);
        }

        testFile.remove();
        testCollection.remove();
        session.save();
    }

    /**
     * Test lock / unlock operations
     */
    @Test
    public void testLock() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession();
        JCRStoreProvider provider = JCRSessionFactory.getInstance()
                .getProvider(providerRoot);
        if (!provider.isLockingAvailable()) {
            return;
        }

        JCRNodeWrapper rootNode = session.getNode(providerRoot);
        session.checkout(rootNode);
        String value = "This is a test";
        String mimeType = "text/plain";

        InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

        String name = "test" + System.currentTimeMillis() + ".txt";
        JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
        nodes.add(testFile.getIdentifier());

        session.save();

        if (testFile.isNodeType("jmix:lockable")) {
            boolean result = testFile.lockAndStoreToken("user");
            testFile.getSession().save();

            assertTrue(providerRoot + " : lockAndStoreToken returned false",
                    result);

            Lock lock = testFile.getLock();
            assertNotNull(providerRoot + " : lock is null", lock);

            try {
                testFile.unlock("user");
            } catch (LockException e) {
                fail(providerRoot + " : unlock failed");
            }
        }

        Lock lock = testFile.lock(false, false);
        assertNotNull(providerRoot + " : Lock is null", lock);
        assertTrue(providerRoot + " : Node not locked", testFile.isLocked());
        testFile.unlock("user");
        assertFalse(providerRoot + " : Node not unlocked", testFile.isLocked());
    }

    /**
     * The following test: 1- creates a page with a list that contains several nodes 2- reorders list children 3- checks if the reordering
     * feature is ok
     *
     * @throws Exception
     */
    @Test
    public void testOrdering() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession();

        logger.debug("Get site node");
        JCRNodeWrapper siteNode = session.getNode(SITECONTENT_ROOT_NODE);
        if (!siteNode.isCheckedOut()) {
            session.checkout(siteNode);
        }

        logger.debug("Create a new page");
        JCRNodeWrapper page = siteNode.addNode(
                "page" + System.currentTimeMillis(), "jnt:page");
            page.setProperty("j:templateName", "simple");

        // create a new collection with several children
        logger.debug("Create a new list");
        JCRNodeWrapper listNode = page.addNode(
                "list" + System.currentTimeMillis(), "jnt:contentList");

        logger.debug("Add children to list");
        List<JCRNodeWrapper> children = new ArrayList<JCRNodeWrapper>();
        for (int i = 0; i < 10; i++) {
            children.add(listNode.addNode("test_child_"
                    + System.currentTimeMillis() + "_" + i));
        }
        session.save();

        // reorder existing ones
        logger.debug("Reorder list children");
        Collections.shuffle(children);
        for (JCRNodeWrapper childNode : children) {
            listNode.orderBefore(childNode.getName(), null);
        }
        session.save();

        // check re-ordoring
        logger.debug("Check new ordering");
        JCRNodeWrapper targetNode = session.getNode(listNode.getPath());
        if (!targetNode.isCheckedOut()) {
            session.checkout(targetNode);
        }

        NodeIterator targetNodeChildren = targetNode.getNodes();
        int index = 0;
        while (targetNodeChildren.hasNext()) {
            JCRNodeWrapper currentTargetChildNode = (JCRNodeWrapper) targetNodeChildren
                    .nextNode();
            JCRNodeWrapper node = children.get(index);
            index++;
            assertEquals(providerRoot + " Bad result: nodes[" + index
                    + "] are different: " + currentTargetChildNode.getPath()
                    + " != " + node.getPath(),
                    currentTargetChildNode.getPath(), node.getPath());
        }
    }

    @Test
    public void testNodeCache() throws LockException, PathNotFoundException,
            ConstraintViolationException, VersionException,
            ItemExistsException, RepositoryException {
        JCRNodeWrapper root = session.getNode(SITECONTENT_ROOT_NODE).addNode(
                "testNodeCache-" + System.currentTimeMillis(),
                "jnt:contentFolder");
        String nodePath = root.getPath();

        JCRNodeWrapper video = root.addNode("video-1", "jnt:video");
        video.setProperty("jcr:title", "Jahia 6.5 Features Overview");
        video.setProperty("height", 400);
        video.setProperty("width", 500);
        video.setProperty("autoplay", true);

        video = root.addNode("video-2", "jnt:video");
        video.setProperty("jcr:title", "Jahia 6.5 Technical Spec");
        video.setProperty("height", 700);
        video.setProperty("width", 800);
        video.setProperty("autoplay", false);

        session.save();
        nodes.add(root.getIdentifier());

        // check the properties
        video = session.getNode(root.getPath() + "/video-1");
        assertTrue("Expected property jcr:title not found",
                video.hasProperty("jcr:title"));
        assertTrue("Expected property height not found",
                video.hasProperty("height"));
        assertTrue("Expected property width not found",
                video.hasProperty("width"));
        assertTrue("Expected property autoplay not found",
                video.hasProperty("autoplay"));

        // remove height property and check it with hasProperty
        session.getNode(root.getPath() + "/video-1").getProperty("height")
                .remove();
        assertFalse(
                "Property height is still there",
                session.getNode(root.getPath() + "/video-1").hasProperty(
                        "height"));
        assertFalse("Property height is still there",
                video.hasProperty("height"));
        session.save();
        assertFalse(
                "Property height is still there",
                session.getNode(root.getPath() + "/video-1").hasProperty(
                        "height"));
        assertFalse("Property height is still there",
                video.hasProperty("height"));

        // remove the width property and check it with hasProperty the other way
        root = session.getNode(nodePath);
        video = session.getNode(root.getPath() + "/video-1");
        JCRNodeWrapper video2 = session.getNode(root.getPath() + "/video-2");
        // populate hasPropertyCache
        assertTrue("Property width is not found", video.hasProperty("width"));
        assertTrue("Property width is not found", video2.hasProperty("width"));

        // remove width for both children accessing them indirectly (via root.getNodes())
        for (NodeIterator iterator = root.getNodes(); iterator.hasNext();) {
            iterator.nextNode().getProperty("width").remove();
        }
        session.save();
        assertFalse(
                "Property width is still there",
                session.getNode(root.getPath() + "/video-1").hasProperty(
                        "width"));
        assertFalse(
                "Property width is still there",
                session.getNode(root.getPath() + "/video-2").hasProperty(
                        "width"));
        // those two check the correctness of hasPropertyCache invalidation
        assertFalse("Property width is still there", video.hasProperty("width"));
        assertFalse("Property width is still there",
                video2.hasProperty("width"));
    }

    @Test
    public void testNodeCopySameNamePropertyAndChild() throws RepositoryException {
        JCRNodeWrapper root = session.getNode(SITECONTENT_ROOT_NODE).addNode(
                "testNodeCopy-source-" + System.currentTimeMillis(),
                "jnt:contentFolder");

        JCRNodeWrapper dest = session.getNode(SITECONTENT_ROOT_NODE).addNode(
                "testNodeCopy-dest-" + System.currentTimeMillis(),
                "jnt:contentFolder");

        JCRNodeWrapper source = root.addNode("source", "test:sameNameChildAndPropertyParent");
        source.setProperty("sameName", "some value");

        session.save();
        nodes.add(root.getIdentifier());
        nodes.add(dest.getIdentifier());

        assertTrue("Expected property sameName not found", source.hasProperty("sameName"));
        assertNotNull("Expected child node sameName not found", source.getNode("sameName"));

        source.copy(dest, "copy", true, null, 500);
        session.save();
        JCRNodeWrapper copy = dest.getNode("copy");
        assertTrue("Expected property sameName not found", copy.hasProperty("sameName"));
        assertNotNull("Expected child node sameName not found", copy.getNode("sameName"));
    }
}
