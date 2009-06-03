package org.jahia.services.content;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.IOUtils;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.impl.jahia.JahiaContentStoreProvider;

import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * This is a test case
 */
public class ContentTest extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        JCRStoreService service = ServicesRegistry.getInstance().getJCRStoreService();
        final Map<String, JCRStoreProvider> mountPoints = service.getMountPoints();
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
                    suite.addTest(new ContentTest(method.getName(),providerRoot));
                }
            }
        }
        return suite;
    }

    private ProcessingContext ctx;
    private String providerRoot;

    public ContentTest(String name, String path) {
        super(name);
        this.providerRoot = path;
    }

    @Override
    protected void setUp() throws Exception {
        ctx = Jahia.getThreadParamBean();
    }

    @Override
    public String getName() {
        return super.getName() + " in " +providerRoot ;
    }

    /**
     * Test creation / deletion of folder
     *
     * @throws RepositoryException
     */
    public void testCreateFolder() throws Exception {
        JCRStoreService service = ServicesRegistry.getInstance().getJCRStoreService();

        JCRSessionWrapper session = service.getThreadSession(ctx.getUser());

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            final String name = "test" + System.currentTimeMillis();

            JCRNodeWrapper testCollection = rootNode.createCollection(name);
            session.save();

            assertTrue(providerRoot + " : Created folder is not valid", testCollection.isValid());
            assertTrue(providerRoot + " : Created folder is not a collection", testCollection.isCollection());

//            long creationDate = testCollection.getCreationDateAsDate().getTime();
//            assertTrue(providerRoot+ " : Creation date invalid", creationDate < System.currentTimeMillis() && creationDate > System.currentTimeMillis()-10000);

//            long lastModifiedDate = testCollection.getLastModifiedAsDate().getTime();
//            assertTrue(providerRoot+ " : Modification date invalid", lastModifiedDate < System.currentTimeMillis() && lastModifiedDate > System.currentTimeMillis()-10000);

            testCollection = session.getNode(providerRoot + "/" + name);

            assertTrue(providerRoot + " : Folder cannot be reloaded", testCollection.isValid());

            testCollection.remove();
            session.save();

            try {
                session.getNode(providerRoot + "/" + name);
                fail(providerRoot + " : Folder has not been deleted");

            } catch (PathNotFoundException e) {
                // ok
            }
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
        JCRStoreService service = ServicesRegistry.getInstance().getJCRStoreService();

        JCRSessionWrapper session = service.getThreadSession(ctx.getUser());

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            String value = "This is a test";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "test" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
            session.save();

            assertTrue(providerRoot + " : Created file is not valid", testFile.isValid());

            assertEquals(providerRoot + " : Size is not the same", value.length(), testFile.getFileContent().getContentLength());
            assertEquals(providerRoot + " : Mime type is not the same", mimeType, testFile.getFileContent().getContentType());

            long creationDate = testFile.getCreationDateAsDate().getTime();
            assertTrue(providerRoot+ " : Creation date invalid", creationDate < System.currentTimeMillis() && creationDate > System.currentTimeMillis()-10000);

            long lastModifiedDate = testFile.getLastModifiedAsDate().getTime();
            assertTrue(providerRoot+ " : Modification date invalid", lastModifiedDate < System.currentTimeMillis() && lastModifiedDate > System.currentTimeMillis()-10000);

            testFile = session.getNode(providerRoot + "/" + name);

            assertTrue(providerRoot + " : File cannot be reloaded", testFile.isValid());

            is = testFile.getFileContent().downloadFile();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(is, baos);
            String checkString = new String(baos.toByteArray(), "UTF-8");
            assertEquals(providerRoot + " : File content is different", value, checkString);

            testFile.remove();
            session.save();

            try {
                session.getNode(providerRoot + "/" + name);
                fail(providerRoot + " : File has not been deleted");

            } catch (PathNotFoundException e) {
                // ok
            }
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
        JCRStoreService service = ServicesRegistry.getInstance().getJCRStoreService();

        JCRSessionWrapper session = service.getThreadSession(ctx.getUser());

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            final String name = "test" + System.currentTimeMillis();

            JCRNodeWrapper testCollection = rootNode.createCollection(name);
            session.save();

            final String value = "Title test";

            testCollection.setProperty("jcr:title", value);
            testCollection.save();

            testCollection = session.getNode(providerRoot + "/" + name);
            try {
                String actual = testCollection.getProperty("jcr:title").getString();
                assertEquals("getProperty() Property value is not the same",value, actual);
            } catch (PathNotFoundException e) {
                fail("getProperty() cannot find property");
            }

            assertEquals("getPropertyAsString() : Property value is not the same",value, testCollection.getPropertyAsString("jcr:title"));
            assertEquals("getPropertiesAsString() : Property value is not the same",value, testCollection.getPropertiesAsString().get("jcr:title"));

            boolean found = false;
            PropertyIterator pi = testCollection.getProperties();
            while (pi.hasNext()) {
                Property p = pi.nextProperty();
                if (p.getName().equals("jcr:title")) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);

            testCollection.remove();
            session.save();

            try {
                session.getNode(providerRoot + "/" + name);
                fail(providerRoot + " : Folder has not been deleted");

            } catch (PathNotFoundException e) {
                // ok
            }
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
        JCRStoreService service = ServicesRegistry.getInstance().getJCRStoreService();

        JCRSessionWrapper session = service.getThreadSession(ctx.getUser());

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            String value = "This is a test";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "test" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);
            session.save();

            final String newname = "renamed" + name;
            boolean result = testFile.renameFile(newname);

            assertTrue("rename returned false", result);

            try {
                session.getNode(providerRoot + "/" + newname);
            } catch (RepositoryException e) {
                fail(providerRoot + " : Renamed file not found");
            }

            testFile.remove();
            session.save();

            try {
                session.getNode(providerRoot + "/" + name);
                fail(providerRoot + " : File has not been deleted");

            } catch (PathNotFoundException e) {
                // ok
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
        JCRStoreService service = ServicesRegistry.getInstance().getJCRStoreService();

        JCRSessionWrapper session = service.getThreadSession(ctx.getUser());

        try {
            JCRNodeWrapper rootNode = session.getNode(providerRoot);

            String value = "This is a test";
            String mimeType = "text/plain";

            InputStream is = new ByteArrayInputStream(value.getBytes("UTF-8"));

            String name = "test" + System.currentTimeMillis() + ".txt";
            JCRNodeWrapper testFile = rootNode.uploadFile(name, is, mimeType);

            final String collectionName = "foldertest" + System.currentTimeMillis();
            JCRNodeWrapper testCollection = rootNode.createCollection(collectionName);

            session.save();

            boolean result = testFile.moveFile(providerRoot + "/" + collectionName);

            assertTrue("move returned false", result);

            try {
                session.getNode(providerRoot + "/" + collectionName);
            } catch (RepositoryException e) {
                fail(providerRoot + " : moved file not found");
            }

            testFile.remove();
            rootNode.getNode(collectionName).remove();
            session.save();

            try {
                session.getNode(providerRoot + "/" + name);
                fail(providerRoot + " : File has not been deleted");

            } catch (PathNotFoundException e) {
                // ok
            }
        } finally {
            session.logout();
        }
    }

}
