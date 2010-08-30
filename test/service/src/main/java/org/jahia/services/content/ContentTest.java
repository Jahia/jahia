/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.log4j.Logger;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * This test unit tests all basic content operations on all connected providers. This is useful to test common
 * functionality across the Entropy and VFS connectors.
 */
@RunWith(Parameterized.class)
public class ContentTest {
    private static final transient Logger logger = Logger.getLogger(ContentTest.class);
    private final static String TESTSITE_NAME = "contentTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> data = new ArrayList<Object[]>();
        final Map<String, JCRStoreProvider> mountPoints = JCRSessionFactory.getInstance().getMountPoints();
        for (String providerRoot : mountPoints.keySet()) {
            if (providerRoot.equals("/")) {
                providerRoot = "/shared";
            }
            Object[] parameter = new Object[1];
            parameter[0] = providerRoot;
            data.add(parameter);
        }
        return data;
    }

    private String providerRoot;

    private static List<String> nodes = new ArrayList<String>();

    public ContentTest(String path) {
        this.providerRoot = path;
    }

    protected java.lang.String getName() {
        return providerRoot;
    }

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        TestHelper.createSite(TESTSITE_NAME);
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                for (String node : nodes) {
                    session.getNodeByIdentifier(node).remove();
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

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            final String name = "test" + System.currentTimeMillis();

            JCRNodeWrapper testCollection = rootNode.createCollection(name);
            session.save();
            nodes.add(testCollection.getIdentifier());

            assertTrue(providerRoot + " : Created folder is not a collection", testCollection.isCollection());

            long creationDate = testCollection.getCreationDateAsDate().getTime();
            assertTrue(providerRoot + " : Creation date invalid", creationDate < System.currentTimeMillis() && creationDate > System.currentTimeMillis() - 10000);

            long lastModifiedDate = testCollection.getLastModifiedAsDate().getTime();
            assertTrue(providerRoot + " : Modification date invalid", lastModifiedDate < System.currentTimeMillis() && lastModifiedDate > System.currentTimeMillis() - 10000);

            testCollection = session.getNode(providerRoot + "/" + name);

        } finally {
            session.logout();
        }
    }

    /**
     * Test file upload
     *
     * @throws RepositoryException
     */
    @Test
    public void testUpload() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

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
                    creationTime < (System.currentTimeMillis() + 600000) && creationTime > (System.currentTimeMillis() - 600000));

            Date lastModifiedDate = testFile.getLastModifiedAsDate();
            assertNotNull(providerRoot + " : Last modified date is null !", lastModifiedDate);
            long lastModifiedTime = lastModifiedDate.getTime();
            assertTrue(providerRoot + " : Modification date invalid",
                    lastModifiedTime < (System.currentTimeMillis() + 600000) && lastModifiedTime > (System.currentTimeMillis() - 600000));

            testFile = session.getNode(providerRoot + "/" + name);

            is = testFile.getFileContent().downloadFile();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(is, baos);
            String checkString = new String(baos.toByteArray(), "UTF-8");
            assertEquals(providerRoot + " : File content is different", value, checkString);
        } finally {
            session.logout();
        }
    }

    /**
     * Test simple property set
     *
     * @throws RepositoryException
     */
    @Test
    public void testSetStringProperty() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            final String name = "test" + System.currentTimeMillis();

            JCRNodeWrapper testCollection = rootNode.createCollection(name);
            session.save();
            nodes.add(testCollection.getIdentifier());

            final String value = "Title test";

            try {
                testCollection.setProperty("jcr:description", value);
                testCollection.save();
            } catch (ConstraintViolationException e) {
                return;
            }

            testCollection = session.getNode(providerRoot + "/" + name);
            try {
                String actual = testCollection.getProperty("jcr:description").getString();
                assertEquals(providerRoot + " : getProperty() Property value is not the same", value, actual);
            } catch (PathNotFoundException e) {
                fail("getProperty() cannot find property");
            }

            assertEquals(providerRoot + " : getPropertyAsString() : Property value is not the same", value,
                    testCollection.getPropertyAsString("jcr:description"));
            assertEquals(providerRoot + " : getPropertiesAsString() : Property value is not the same", value,
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
        } finally {
            session.logout();
        }

    }

    /**
     * Test rename of a file
     *
     * @throws RepositoryException
     */
    @Test
    public void testRename() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            String value = "This is a test";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "test" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
            session.save();
            nodes.add(testFile.getIdentifier());

            final String newname = "renamed" + name;
            boolean result = false;
            try {
                result = testFile.rename(newname);

                assertTrue(providerRoot + " : rename returned false", result);

                try {
                    testFile = session.getNode(providerRoot + "/" + newname);
                } catch (RepositoryException e) {
                    fail(providerRoot + " : Renamed file not found");
                }
            } catch (UnsupportedRepositoryOperationException e) {

            }
        } finally {
            session.logout();
        }
    }

    /**
     * Test move of a file
     *
     * @throws RepositoryException
     */
    @Test
    public void testMove() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            String value = "This is a test";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "test" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
            nodes.add(testFile.getIdentifier());

            final String collectionName = "foldertest" + System.currentTimeMillis();
            JCRNodeWrapper testCollection = rootNode.createCollection(collectionName);
            nodes.add(testCollection.getIdentifier());

            session.save();

            try {
                session.move(testFile.getPath(), providerRoot + "/" + collectionName + "/" + testFile.getName());
            } catch (RepositoryException e) {
                fail(providerRoot + " : move throwed exception :" + e);
            }


            try {
                session.getNode(providerRoot + "/" + collectionName);
            } catch (RepositoryException e) {
                fail(providerRoot + " : moved file not found");
            }

        } finally {
            session.logout();
        }
    }

    /**
     * Test lock / unlock operations
     */
    @Test
    public void testLock() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRStoreProvider provider = JCRSessionFactory.getInstance().getProvider(providerRoot);
        if (!provider.isLockingAvailable()) {
            return;
        }

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            String value = "This is a test";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "test" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
            nodes.add(testFile.getIdentifier());

            session.save();

            if (testFile.isNodeType("jmix:lockable")) {
                boolean result = testFile.lockAndStoreToken("user");
                testFile.save();

                assertTrue(providerRoot + " : lockAndStoreToken returned false", result);

                Lock lock = testFile.getLock();
                assertNotNull(providerRoot + " : lock is null", lock);

                try {
                    testFile.unlock();
                } catch (LockException e) {
                    fail(providerRoot + " : unlock failed");
                }
            }

            Lock lock = testFile.lock(false, false);
            assertNotNull(providerRoot + " : Lock is null", lock);
            assertTrue(providerRoot + " : Node not locked", testFile.isLocked());
            testFile.unlock();
            assertFalse(providerRoot + " : Node not unlocked", testFile.isLocked());
        } finally {
            session.logout();
        }

    }

    /**
     * Test file search
     *
     * @throws RepositoryException
     */
    @Test
    public void testSearch() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRStoreProvider provider = JCRSessionFactory.getInstance().getProvider(providerRoot);
        if (!provider.isSearchAvailable()) {
            return;
        }

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            String value = "123456789abcd 123abc 456bcd 789def 123456789abcd";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "testSearch" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
            session.save();
            // nodes.add(testFile.getIdentifier());

            // Do the query
            QueryManager qm = JCRSessionFactory.getInstance().getCurrentUserSession().getWorkspace().getQueryManager();
            Query query = qm.createQuery("select * from [jnt:file] as f where contains(f.[jcr:content], '456bcd')",
                    Query.JCR_SQL2);
            QueryResult queryResult = query.execute();
            RowIterator it = queryResult.getRows();
            int resultCount = 0;
            while (it.hasNext()) {
                Row row = it.nextRow();
                resultCount++;
                String path = row.getValue(JcrConstants.JCR_PATH).getString();
                assertEquals(providerRoot + " : Wrong file found ('" + path + "' instead of '" + testFile.getPath() + "')",
                        testFile.getPath(), path);
            }
            assertEquals(providerRoot + " : Invalid number of results returned by query", 1, resultCount);

            Node removeTestFile = session.getNodeByIdentifier(testFile.getIdentifier());
            removeTestFile.remove();
            session.save();
        } finally {
            session.logout();
        }
    }

    /**
     * The following test:
     * 1- creates a page with a list that contains several nodes
     * 2- reorders list children
     * 3- checks if the reordering feature is ok
     *
     * @throws Exception
     */
    @Test
    public void testOrdering() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        try {
            logger.debug("Get site node");
            JCRNodeWrapper siteNode = session.getNode(SITECONTENT_ROOT_NODE);
            if (!siteNode.isCheckedOut()) {
                session.checkout(siteNode);
            }

            logger.debug("Create a new page");
            JCRNodeWrapper page = siteNode.addNode("page" + System.currentTimeMillis(), "jnt:page");

            // create a new collection with several children
            logger.debug("Create a new list");
            JCRNodeWrapper listNode = page.addNode("list" + System.currentTimeMillis(), "jnt:contentList");


            logger.debug("Add children to list");
            List<JCRNodeWrapper> children = new ArrayList<JCRNodeWrapper>();
            for (int i = 0; i < 10; i++) {
                children.add(listNode.addNode("test_child_" + System.currentTimeMillis() + "_" + i));
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
                JCRNodeWrapper currentTargetChildNode = (JCRNodeWrapper) targetNodeChildren.nextNode();
                JCRNodeWrapper node = children.get(index);
                index++;
                assertEquals(providerRoot + " Bad result: nodes[" + index + "] are different: " + currentTargetChildNode.getPath() + " != " + node.getPath(), currentTargetChildNode.getPath(), node.getPath());
            }

        } finally {
            session.logout();
        }
    }

    /**
     * Test referencing a provider file node inside the main repository.
     *
     * @throws RepositoryException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testReferencing() throws RepositoryException, UnsupportedEncodingException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        try {
            JCRNodeWrapper siteNode = session.getNode(SITECONTENT_ROOT_NODE);
            if (!siteNode.isCheckedOut()) {
                session.checkout(siteNode);
            }

            JCRNodeWrapper rootNode = session.getNode(providerRoot);
            String value = "This is a test";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "test" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
            nodes.add(testFile.getIdentifier());

            logger.debug("Create a new page");
            JCRNodeWrapper page = siteNode.addNode("page" + System.currentTimeMillis(), "jnt:page");
            logger.debug("Create a new list");
            JCRNodeWrapper listNode = page.addNode("list" + System.currentTimeMillis(), "jnt:contentList");

            JCRNodeWrapper refNode = listNode.addNode("ref" + +System.currentTimeMillis(), "jnt:fileReference");
            refNode.setProperty("j:node", testFile);

            session.save();

            PropertyIterator refPropertyIterator = testFile.getWeakReferences();
            int resultCount = 0;
            while (refPropertyIterator.hasNext()) {
                resultCount++;
                Property refProperty = refPropertyIterator.nextProperty();
                assertEquals("Reference property name is invalid !", "j:node", refProperty.getName());
                assertNotNull("Reference node is null", refProperty.getNode());
                assertEquals("Reference identifier is invalid !", testFile.getIdentifier(), refProperty.getNode().getIdentifier());
            }
            assertEquals("Invalid number of file references !", 1, resultCount);

        } finally {
            if (session != null) {
                session.logout();
            }

        }
    }
}
