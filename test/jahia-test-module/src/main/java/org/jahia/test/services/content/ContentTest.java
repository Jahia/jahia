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
package org.jahia.test.services.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import javax.jcr.version.VersionException;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.jahia.ajax.gwt.client.data.GWTJahiaSearchQuery;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.helper.NavigationHelper;
import org.jahia.ajax.gwt.helper.SearchHelper;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;

/**
 * This test unit tests all basic content operations on all connected providers. This is useful to test common functionality across the
 * Entropy and VFS connectors.
 */
@RunWith(Parameterized.class)
public class ContentTest {
    private static final transient Logger logger = org.slf4j.LoggerFactory
            .getLogger(ContentTest.class);
    private final static String TESTSITE_NAME = "contentTestSite";
    private final static String SITECONTENT_ROOT_NODE = "/sites/"
            + TESTSITE_NAME;
    private static final String SIMPLE_REFERENCE_PROPERTY_NAME = "test:simpleNode";
    private static final String MULTIPLE_I18N_REFERENCE_PROPERTY_NAME = "test:multipleI18NNode";
    private static final String TEST_EXTERNAL_REFERENCE_NODE_TYPE = "test:externalReference";

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Collection<Object[]> data = new ArrayList<Object[]>();
        final Map<String, JCRStoreProvider> mountPoints = JCRSessionFactory
                .getInstance().getMountPoints();
        for (String providerRoot : mountPoints.keySet()) {
            if (providerRoot.equals("/")) {
                providerRoot = "/sites/systemsite";
            } else if (providerRoot.startsWith("/modules/")
                    || providerRoot.startsWith("/external-")
                    || providerRoot.startsWith("/users")
                    || providerRoot.startsWith("/groups")) {
                continue;
            }
            Object[] parameter = new Object[1];
            logger.info("Added " + providerRoot + " to parameterized ContentTest");
            parameter[0] = providerRoot;
            data.add(parameter);
        }
        return data;
    }

    private String providerRoot;

    private static List<String> nodes = new ArrayList<String>();

    private JCRSessionWrapper session;

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
     * Test file search
     *
     * @throws RepositoryException
     */
    @Test
    public void testSearch() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRStoreProvider provider = JCRSessionFactory.getInstance()
                .getProvider(providerRoot);
        if (!provider.isSearchAvailable()) {
            return;
        }
        JCRNodeWrapper testFile = null;
        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);
            session.checkout(rootNode);
            String value = "123456789abcd 123abc 456bcd 789def 123456789abcd";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "testSearch" + System.currentTimeMillis() + ".txt";
            testFile = rootNode.uploadFile(name, is, mimeType);
            session.save();
            TestHelper.triggerScheduledJobsAndWait();
            // nodes.add(testFile.getIdentifier());

            // Do the query
            QueryManager qm = JCRSessionFactory.getInstance()
                    .getCurrentUserSession().getWorkspace().getQueryManager();
            Query query = qm
                    .createQuery(
                            "select * from [jnt:file] as f where contains(f.*, '456bcd')",
                            Query.JCR_SQL2);
            QueryResult queryResult = query.execute();
            RowIterator it = queryResult.getRows();
            int resultCount = 0;
            while (it.hasNext()) {
                Row row = it.nextRow();
                resultCount++;
                String path = row.getValue(JcrConstants.JCR_PATH).getString();
                assertEquals(providerRoot + " : Wrong file found ('" + path
                        + "' instead of '" + testFile.getPath() + "')",
                        testFile.getPath(), path);
            }
            assertEquals(providerRoot
                    + " : Invalid number of results returned by query", 1,
                    resultCount);

            // now let's use our search service to do the same query.
            GWTJahiaSearchQuery gwtJahiaSearchQuery = new GWTJahiaSearchQuery();
            gwtJahiaSearchQuery.setQuery("456bcd");
            gwtJahiaSearchQuery.setInFiles(true);
            gwtJahiaSearchQuery.setOriginSiteUuid(null);
            gwtJahiaSearchQuery.setPages(null);
            gwtJahiaSearchQuery.setLanguage(null);
            SearchHelper searchHelper = (SearchHelper) SpringContextSingleton
                    .getInstance().getContext().getBean("SearchHelper");
            List<GWTJahiaNode> result = searchHelper.search(
                    gwtJahiaSearchQuery, 0, 0, false, null, session);
            assertEquals("Invalid number of results for query ", 1,
                    result.size());
            String path = result.iterator().next().getPath();
            assertEquals(providerRoot + " : Wrong file found ('" + path
                    + "' instead of '" + testFile.getPath() + "')",
                    testFile.getPath(), path);
        } finally {
            if (testFile != null) {
                try {
                    Node removeTestFile = session.getNodeByIdentifier(testFile
                            .getIdentifier());
                    removeTestFile.remove();
                    session.save();
                } catch (Exception e) {
                    logger.warn("Cant remove uploaded file", e);
                }
            }
        }
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

    /**
     * Test referencing a provider file node inside the main repository.
     *
     * @throws RepositoryException
     * @throws UnsupportedEncodingException
     */
    @Test
    public void testReferencing() throws RepositoryException,
            UnsupportedEncodingException, GWTJahiaServiceException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession(null, Locale.ENGLISH);
        JCRNodeWrapper siteNode = session.getNode(SITECONTENT_ROOT_NODE);
        if (!siteNode.isCheckedOut()) {
            session.checkout(siteNode);
        }

        JCRNodeWrapper rootNode = session.getNode(providerRoot);
        session.checkout(rootNode);
        String value = "This is a test";
        String mimeType = "text/plain";

        InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

        String name1 = "test1_" + System.currentTimeMillis() + ".txt";
        JCRNodeWrapper testFile1 = rootNode.uploadFile(name1, is, mimeType);
        nodes.add(testFile1.getIdentifier());
        String name2 = "test2_" + System.currentTimeMillis() + ".txt";
        JCRNodeWrapper testFile2 = rootNode.uploadFile(name2, is, mimeType);
        nodes.add(testFile2.getIdentifier());

        logger.debug("Create a new page");
        JCRNodeWrapper page = siteNode.addNode(
                "page" + System.currentTimeMillis(), "jnt:page");
        page.setProperty("jcr:title", "test");
        page.setProperty("j:templateName", "simple");
        logger.debug("Create a new list");
        JCRNodeWrapper listNode = page.addNode(
                "list" + System.currentTimeMillis(), "jnt:contentList");

        JCRNodeWrapper refNode = listNode.addNode(
                "ref" + +System.currentTimeMillis(),
                TEST_EXTERNAL_REFERENCE_NODE_TYPE);
        refNode.setProperty(SIMPLE_REFERENCE_PROPERTY_NAME, testFile1);

        session.save();

        Property refProperty = refNode
                .getProperty(SIMPLE_REFERENCE_PROPERTY_NAME);
        assertNotNull(providerRoot
                + " : Referenced external node is not available !",
                refProperty.getNode());

        refNode.setProperty(SIMPLE_REFERENCE_PROPERTY_NAME, testFile1);

        session.save();

        // now some global check to make sure implementation is complete.

        refProperty = refNode.getProperty(SIMPLE_REFERENCE_PROPERTY_NAME);
        assertNotNull(providerRoot
                + " : Referenced external node is not available !",
                refProperty.getNode());

        boolean hasRefProperty = refNode
                .hasProperty(SIMPLE_REFERENCE_PROPERTY_NAME);
        assertTrue(
                providerRoot
                        + " : hasProperty method is not returning expected true value for reference property",
                hasRefProperty);

        PropertyIterator propertyIterator = refNode.getProperties();
        Property foundReferenceProperty = null;
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();
            if (SIMPLE_REFERENCE_PROPERTY_NAME.equals(property.getName())) {
                foundReferenceProperty = property;
            }
        }
        assertNotNull(
                providerRoot
                        + " : Didn't find reference property using the getProperties() call",
                foundReferenceProperty);
        assertEquals(
                providerRoot
                        + " : Identifier on property found with getProperties() call is not valid !",
                foundReferenceProperty.getNode().getIdentifier(),
                testFile1.getIdentifier());

        PropertyIterator refPropertyIterator = testFile1.getWeakReferences();
        int resultCount = 0;
        while (refPropertyIterator.hasNext()) {
            resultCount++;
            refProperty = refPropertyIterator.nextProperty();
            assertEquals(providerRoot
                    + " : Reference property name is invalid !",
                    SIMPLE_REFERENCE_PROPERTY_NAME, refProperty.getName());
            assertNotNull(providerRoot + " : Reference node is null",
                    refProperty.getNode());
            assertEquals(providerRoot + " : Reference identifier is invalid !",
                    testFile1.getIdentifier(), refProperty.getNode()
                            .getIdentifier());
        }
        assertEquals(providerRoot + " : Invalid number of file references !",
                1, resultCount);

        NavigationHelper navigationHelper = (NavigationHelper) SpringContextSingleton
                .getInstance().getContext().getBean("NavigationHelper");
        List<String> paths = new ArrayList<String>();
        paths.add(testFile1.getPath());
        List<GWTJahiaNodeUsage> usages = navigationHelper.getUsages(paths,
                session, Locale.getDefault());
        assertEquals(providerRoot + " : Invalid number of file usages !", 1,
                usages.size());
        GWTJahiaNodeUsage firstUsage = usages.iterator().next();
        assertEquals(providerRoot
                + " : Expected path for node pointing to file is invalid !",
                refNode.getPath(), firstUsage.getPath());

        // now let's test property removal, to check if it works.
        refProperty = refNode.getProperty(SIMPLE_REFERENCE_PROPERTY_NAME);
        refProperty.remove();
        session.save();

        assertFalse(
                providerRoot
                        + " : Property should no longer be present since it was removed !",
                refNode.hasProperty(SIMPLE_REFERENCE_PROPERTY_NAME));

        refNode.setProperty(SIMPLE_REFERENCE_PROPERTY_NAME, testFile1);
        ValueFactory valueFactory = session.getValueFactory();
        Value[] multipleRefValues = new Value[] {
                valueFactory.createValue(testFile1),
                valueFactory.createValue(testFile2) };
        refNode.setProperty(MULTIPLE_I18N_REFERENCE_PROPERTY_NAME,
                multipleRefValues);
        session.save();

        Value[] resultingMultipleRefValues = refNode.getProperty(
                MULTIPLE_I18N_REFERENCE_PROPERTY_NAME).getValues();
        assertEquals(
                providerRoot
                        + " : Read count of multiple reference values is not equal to set values",
                multipleRefValues.length, resultingMultipleRefValues.length);
        for (int i = 0; i < resultingMultipleRefValues.length; i++) {
            Value resultingMultipleRefValue = resultingMultipleRefValues[i];
            Value multipleRefValue = multipleRefValues[i];
            assertTrue(
                    providerRoot
                            + " : Read multiple reference values not equal to set values",
                    multipleRefValue.getString().equals(
                            resultingMultipleRefValue.getString()));
        }

        // now we remove one of the two references.
        Value[] singleRefValues = new Value[] { valueFactory
                .createValue(testFile2) };
        refNode.setProperty(MULTIPLE_I18N_REFERENCE_PROPERTY_NAME,
                singleRefValues);
        session.save();

        Value[] resultingSingleRefValues = refNode.getProperty(
                MULTIPLE_I18N_REFERENCE_PROPERTY_NAME).getValues();
        assertEquals(
                providerRoot
                        + " : Read count of single reference values is not equal to set values",
                singleRefValues.length, resultingSingleRefValues.length);
        for (int i = 0; i < resultingSingleRefValues.length; i++) {
            Value resultingSingleRefValue = resultingSingleRefValues[i];
            Value singleRefValue = singleRefValues[i];
            assertTrue(
                    providerRoot
                            + " : Read single reference values not equal to set values",
                    singleRefValue.getString().equals(
                            resultingSingleRefValue.getString()));
        }

        // now let's remove everything and make sure that it looks ok.
        Property singleRefProperty = refNode
                .getProperty(SIMPLE_REFERENCE_PROPERTY_NAME);
        singleRefProperty.remove();
        Property multipleI18NRefProperty = refNode
                .getProperty(MULTIPLE_I18N_REFERENCE_PROPERTY_NAME);
        multipleI18NRefProperty.remove();
        session.save();

        paths.clear();
        paths.add(testFile1.getPath());
        paths.add(testFile2.getPath());
        usages = navigationHelper
                .getUsages(paths, session, Locale.getDefault());
        assertEquals(providerRoot
                + " : Usages should be empty but they are not !" + usages, 0,
                usages.size());
        assertFalse(providerRoot
                + " : single reference property should no longer exist !",
                refNode.hasProperty(SIMPLE_REFERENCE_PROPERTY_NAME));
        assertFalse(providerRoot
                + " : multiple reference property should no longer exist !",
                refNode.hasProperty(MULTIPLE_I18N_REFERENCE_PROPERTY_NAME));
    }

    @Test
    public void testNavigation() throws RepositoryException,
            GWTJahiaServiceException {
        final Map<String, JCRStoreProvider> mountPoints = JCRSessionFactory
                .getInstance().getMountPoints();

        List<String> mountLocations = new ArrayList<String>();
        for (String providerRoot : mountPoints.keySet()) {
            if (providerRoot.startsWith("/mounts")) {
                mountLocations.add(providerRoot);
            }
        }

        JCRSessionWrapper session = JCRSessionFactory.getInstance()
                .getCurrentUserSession();
        JCRSiteNode siteNode = (JCRSiteNode) session
                .getNode(SITECONTENT_ROOT_NODE);
        NavigationHelper navigationHelper = (NavigationHelper) SpringContextSingleton
                .getInstance().getContext().getBean("NavigationHelper");
        List<String> paths = new ArrayList<String>();
        paths.add("/mounts");
        List<GWTJahiaNode> rootNodes = navigationHelper.retrieveRoot(paths,
                null, null, null, null, null, null, siteNode, session,
                Locale.ENGLISH);
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
            List<GWTJahiaNode> childNodes = navigationHelper.ls(rootNode.getPath(),
                    nodeTypes, new ArrayList<String>(),
                    new ArrayList<String>(), fields, session,
                    Locale.getDefault());
            assertEquals(
                    "Mounted providers in /mounts does not to correspond to expected amount",
                    mountLocations.size(), childNodes.size());
            for (GWTJahiaNode childNode : childNodes) {
                assertTrue("Path to mount location does not correspond ! Path="
                        + childNode.getPath(),
                        mountLocations.contains(childNode.getPath()));
                assertGWTJahiaNode(childNode, "/mounts/" + childNode.getName());
                List<GWTJahiaNode> childChildNodes = navigationHelper.ls(
                        childNode.getPath(), nodeTypes, new ArrayList<String>(),
                        new ArrayList<String>(), fields, session,
                        Locale.getDefault());
                for (GWTJahiaNode childChildNode : childChildNodes) {
                    assertGWTJahiaNode(childChildNode,
                            "/mounts/" + childNode.getName() + "/"
                                    + childChildNode.getName());
                }
            }
        }
    }

    private void assertGWTJahiaNode(GWTJahiaNode jahiaGWTNode,
            String expectedPath) {
        assertEquals("Expected path and actual GWT node path are not equal !",
                expectedPath, jahiaGWTNode.getPath());
        int lastSlashPosInPath = jahiaGWTNode.getPath().lastIndexOf("/");
        if (lastSlashPosInPath > -1)
            assertEquals("Last part of path and name are not equal !",
                    jahiaGWTNode.getPath().substring(lastSlashPosInPath + 1),
                    jahiaGWTNode.getName());
        else {
            assertEquals("Last part of path and name are not equal !",
                    jahiaGWTNode.getPath(), jahiaGWTNode.getName());
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
    public void testSpecialCharactersInNodeName() throws Exception {
        JCRNodeWrapper root = session.getNode(SITECONTENT_ROOT_NODE);

        try {
            root.addNode("[]*|/%", "jnt:contentFolder");
        } catch (RepositoryException re) {
            assertEquals("Failed to resolve path []*|/% relative to node /sites/contentTestSite", re.getMessage());
        }

        JCRNodeWrapper newNode = root.addNode("..", "jnt:contentFolder");
        assertEquals("/sites[2]", newNode.getPath());

        try {
            root.addNode(".", "jnt:contentFolder");
        } catch (RepositoryException re) {
            assertEquals("No child node definition for contentTestSite found in node /sites", re.getMessage());
        }

    }
}
