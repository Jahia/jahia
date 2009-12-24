package org.jahia.services.content;

import junit.framework.TestCase;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Test of the import export of the schmurtz function
 *
 * @author loom
 *         Date: Dec 18, 2009
 *         Time: 12:07:48 PM
 */
public class SchmurtzTest extends TestCase {
    private JahiaSite site;
    private ProcessingContext ctx;
    private final static String TESTSITE_NAME = "jcrSessionWrapperTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private final static String SCHMURTZ_HOME = "/schmurtzs";
    private final static String PAGESCHMURTZ_HOME_NODE_PATH = "/schmurtzs/jnt_page";
    private final static String TEST_SCHMURTZ_PATH = "/schmurtzs/jnt_page/3pageschmurtz/j:target";

    @Override
    protected void setUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME);
        ctx = Jahia.getThreadParamBean();
        assertNotNull(site);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
    }


    public void testImportXML() throws RepositoryException, IOException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        ImportExportService importExportService = ServicesRegistry.getInstance().getImportExportService();
        JCRNodeWrapper stageRootNode = session
                .getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper stageNode = (JCRNodeWrapper) stageRootNode
                .getNode("home");
        InputStream importStream = getClass().getClassLoader()
                .getResourceAsStream("imports/import-schmurtz.xml");
        session.importXML("/", importStream,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
        importStream.close();
        session.save();

        JCRNodeWrapper testSchmurtzNode = session.getNode(TEST_SCHMURTZ_PATH);

        // now let's use the schmurtz to create content.
        assertTrue("Error while instantiating schmurtz", testSchmurtzNode.copyFile(stageNode, "newTestSchmurtz"));

        session.save();

        // now we need to validate schmurtz instance.
        assertNotNull("Could not find instantiated schmurtz page", session.getNode("/sites/jcrSessionWrapperTest/home/newTestSchmurtz/page1"));

        // now let's clean up after the test.
        JCRNodeWrapper schmurtzTestNode = session.getNode(PAGESCHMURTZ_HOME_NODE_PATH + "/3pageschmurtz");
        schmurtzTestNode.remove();

        // no need to clean up inside the site, since we will remove it completely anyway.
    }

    private void createSubPages(Node currentNode, int level, int nbChildren) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
        if (level <= 0) return;
        for (int i=0; i < nbChildren; i++) {
            Node newSubPage = currentNode.addNode("child" + Integer.toString(i), "jnt:page");
            createSubPages(newSubPage, level-1, nbChildren);
        }
    }


    private Object getNode(Document jdomDocument, String xPathExpression, String prefix) throws JDOMException {
        Element rootElement = jdomDocument.getRootElement();
        String namespaceURI = rootElement.getNamespaceURI();
        XPath objectXPath = XPath.newInstance(xPathExpression);
        objectXPath.addNamespace(prefix, namespaceURI);
        return objectXPath.selectSingleNode(jdomDocument);
    }

    private void checkOnlyOneElement(Document jdomDocument, String xPathExpression, String prefix) throws JDOMException {
        Element rootElement = jdomDocument.getRootElement();
        String namespaceURI = rootElement.getNamespaceURI();
        XPath contextParamXPath = XPath.newInstance(xPathExpression);
        contextParamXPath.addNamespace(prefix, namespaceURI);
        List contextParamList = contextParamXPath.selectNodes(jdomDocument);
        assertEquals(1, contextParamList.size());
    }

    private void assertAllTextEquals(Document jdomDocument, String xPathExpression, String prefix, String value) throws JDOMException {
        Element rootElement = jdomDocument.getRootElement();
        String namespaceURI = rootElement.getNamespaceURI();
        XPath contextParamXPath = XPath.newInstance(xPathExpression);
        contextParamXPath.addNamespace(prefix, namespaceURI);
        List resultList = contextParamXPath.selectNodes(jdomDocument);
        for (Object currentObject : resultList) {
            if (currentObject instanceof Attribute) {
                assertEquals(value, ((Attribute) currentObject).getValue());
            } else if (currentObject instanceof Element) {
                assertEquals(value, ((Element) currentObject).getText());
            } else if (currentObject instanceof Text) {
                assertEquals(value, ((Text) currentObject).getValue());
            } else if (currentObject instanceof Comment) {
                assertEquals(value, ((Comment) currentObject).getText());
            } else if (currentObject instanceof CDATA) {
                assertEquals(value, ((CDATA) currentObject).getText());
            } else if (currentObject instanceof ProcessingInstruction) {
                assertEquals(value, ((ProcessingInstruction) currentObject).getValue());
            } else {
                // default fall-back comparison, should rarely be useful.
                assertEquals(value, currentObject.toString());
            }
        }
    }

    public void testExportDocumentView() throws RepositoryException, IOException, JDOMException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper siteRootNode = session
                .getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper siteHomePageNode = (JCRNodeWrapper) siteRootNode
                .getNode("home");
        JCRNodeWrapper schmurtzHomeNode = (JCRNodeWrapper) session.getNode(SCHMURTZ_HOME);
        JCRNodeWrapper pageSchmurtzHomeNode = (JCRNodeWrapper) session.getNode(PAGESCHMURTZ_HOME_NODE_PATH);

        // we need to create some content to export, that we can check after export.
        createSubPages(siteHomePageNode, 5, 2);

        JCRNodeWrapper newSchmurtzNode = pageSchmurtzHomeNode.addNode("newTestSchmurtz", "jnt:schmurtz");
        newSchmurtzNode.setProperty("j:targetReference", siteHomePageNode);

        String targetNode = newSchmurtzNode.getProperty("j:targetReference").getNode().getPath();
        session.checkout(newSchmurtzNode);
        newSchmurtzNode.getProperty("j:targetReference").remove();
        session.save();
        session.getWorkspace().copy(targetNode, newSchmurtzNode.getPath() + "/j:target");
        // logger.info("Schmurtz is created with the name '" + newSchmurtzNode.getName() + "' for target node " + targetNode);

        session.save();

        FileOutputStream fileOutputStream = new FileOutputStream("schmurtz-export-test.xml");
        session.exportDocumentView(SCHMURTZ_HOME, fileOutputStream, true, false);
        fileOutputStream.close();

        // now we need to validate export.
        SAXBuilder saxBuilder = new SAXBuilder();
        Document jdomDocument = saxBuilder.build("schmurtz-export-test.xml");
        String prefix = "";
        String tagPre = ("".equals(prefix) ? "" : prefix + ":");
        checkOnlyOneElement(jdomDocument, "/schmurtzs/jnt_page/newTestSchmurtz", prefix);

        // now let's clean up.
        newSchmurtzNode = session.getNode(PAGESCHMURTZ_HOME_NODE_PATH + "/newTestSchmurtz");
        newSchmurtzNode.remove();
        session.save();

        // no need to clean up inside the site, since we will remove it completely anyway.        
    }

    @Override
    protected void tearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
        session.logout();
    }
    
}
