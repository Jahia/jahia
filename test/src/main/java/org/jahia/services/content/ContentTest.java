package org.jahia.services.content;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.impl.jahia.JahiaContentStoreProvider;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is a test case
 */
public class ContentTest extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        final Map<String, JCRStoreProvider> mountPoints = JCRSessionFactory.getInstance().getMountPoints();
        for (String providerRoot : mountPoints.keySet()) {
            if (mountPoints.get(providerRoot) instanceof JahiaContentStoreProvider) {
                continue;
            }
            if (providerRoot.equals("/")) {
                providerRoot = "/content/shared";
            }
            Method[] methods = ContentTest.class.getMethods();
            for (Method method : methods) {
                if (method.getName().startsWith("test")) {
                    suite.addTest(new ContentTest(method.getName(), providerRoot));
                }
            }
        }
        return suite;
    }

    private ProcessingContext ctx;
    private String providerRoot;

    private List<String> nodes = new ArrayList();

    public ContentTest(String name, String path) {
        super(name);
        this.providerRoot = path;
    }

    @Override
    protected void setUp() throws Exception {
        ctx = Jahia.getThreadParamBean();
    }

    @Override
    protected void tearDown() throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                for (String node : nodes) {
                    session.getNodeByUUID(node).remove();
                }
                session.save();
                return null;
            }
        });
        nodes.clear();
    }

    @Override
    public String getName() {
        return super.getName() + " in " + providerRoot;
    }

    /**
     * Test creation / deletion of folder
     *
     * @throws RepositoryException
     */
    public void testCreateFolder() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            final String name = "test" + System.currentTimeMillis();

            JCRNodeWrapper testCollection = rootNode.createCollection(name);
            session.save();
            nodes.add(testCollection.getUUID());

            assertTrue(providerRoot + " : Created folder is not a collection", testCollection.isCollection());

//            long creationDate = testCollection.getCreationDateAsDate().getTime();
//            assertTrue(providerRoot+ " : Creation date invalid", creationDate < System.currentTimeMillis() && creationDate > System.currentTimeMillis()-10000);

//            long lastModifiedDate = testCollection.getLastModifiedAsDate().getTime();
//            assertTrue(providerRoot+ " : Modification date invalid", lastModifiedDate < System.currentTimeMillis() && lastModifiedDate > System.currentTimeMillis()-10000);

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
            nodes.add(testFile.getUUID());

            assertEquals(providerRoot + " : Size is not the same", value.length(),
                         testFile.getFileContent().getContentLength());
            assertEquals(providerRoot + " : Mime type is not the same", mimeType,
                         testFile.getFileContent().getContentType());

            long creationDate = testFile.getCreationDateAsDate().getTime();
            assertTrue(providerRoot + " : Creation date invalid",
                       creationDate < (System.currentTimeMillis() + 600000) && creationDate > (System.currentTimeMillis() - 600000));

            long lastModifiedDate = testFile.getLastModifiedAsDate().getTime();
            assertTrue(providerRoot + " : Modification date invalid",
                       lastModifiedDate < (System.currentTimeMillis() + 600000) && lastModifiedDate > (System.currentTimeMillis() - 600000));

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
    public void testSetStringProperty() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            final String name = "test" + System.currentTimeMillis();

            JCRNodeWrapper testCollection = rootNode.createCollection(name);
            session.save();
            nodes.add(testCollection.getUUID());

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
                assertEquals("getProperty() Property value is not the same", value, actual);
            } catch (PathNotFoundException e) {
                fail("getProperty() cannot find property");
            }

            assertEquals("getPropertyAsString() : Property value is not the same", value,
                         testCollection.getPropertyAsString("jcr:description"));
            assertEquals("getPropertiesAsString() : Property value is not the same", value,
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
            nodes.add(testFile.getUUID());

            final String newname = "renamed" + name;
            boolean result = false;
            try {
                result = testFile.renameFile(newname);

                assertTrue("rename returned false", result);

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
    public void testMove() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            String value = "This is a test";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "test" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
            nodes.add(testFile.getUUID());

            final String collectionName = "foldertest" + System.currentTimeMillis();
            JCRNodeWrapper testCollection = rootNode.createCollection(collectionName);
            nodes.add(testCollection.getUUID());

            session.save();

            boolean result = testFile.moveFile(providerRoot + "/" + collectionName);

            assertTrue("move returned false", result);

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
    public void testLock() throws Exception {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            String value = "This is a test";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "test" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
            nodes.add(testFile.getUUID());

            session.save();

            if (testFile.isNodeType("jmix:lockable")) {
                boolean result = testFile.lockAndStoreToken();
                testFile.save();

                assertTrue("lockAndStoreToken returned false", result);

                Lock lock = testFile.getLock();
                assertNotNull("lock is null", lock);

                result = testFile.forceUnlock();
                testFile.save();

                assertTrue("forceUnlock returned false", result);
            }

            Lock lock = testFile.lock(false, false);
            assertNotNull("Lock is null", lock);
            assertTrue("Node not locked", testFile.isLocked());
            testFile.unlock();
            assertFalse("Node not unlocked", testFile.isLocked());
        } finally {
            session.logout();
        }

    }

    /**
     * Test file upload
     *
     * @throws RepositoryException
     */
    public void testSearch() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            String value = "123456789abcd 123abc 456bcd 789def 123456789abcd";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "testSearch" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
            session.save();
            nodes.add(testFile.getIdentifier());

            // Do the query
            QueryManager qm = JCRSessionFactory.getInstance().getCurrentUserSession().getWorkspace().getQueryManager();
            Query query = qm.createQuery("select * from [jnt:file] as f where contains(f.[jcr:content], '456bcd')",
                                         Query.JCR_SQL2);
            QueryResult queryResult = query.execute();
            RowIterator it = queryResult.getRows();
            assertTrue("Bad result number (" + it.getSize() + " instead of 1)", (it.getSize() == 1));
            while (it.hasNext()) {
                Row row = it.nextRow();
                String path = row.getValue(JcrConstants.JCR_PATH).getString();
                assertEquals("Wrong file found ('" + path + "' instead of '" + testFile.getPath() + "')",
                             testFile.getPath(), path);
            }
        } finally {
            session.logout();
        }
    }
}
