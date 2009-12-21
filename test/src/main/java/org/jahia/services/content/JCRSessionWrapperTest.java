package org.jahia.services.content;

import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.custommonkey.xmlunit.*;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TODO Comment me
 *
 * @author loom
 *         Date: Dec 18, 2009
 *         Time: 12:07:48 PM
 */
public class JCRSessionWrapperTest extends XMLTestCase {
    private JahiaSite site;
    private ProcessingContext ctx;
    private final static String TESTSITE_NAME = "jcrSessionWrapperTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @Override
    protected void setUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME);
        ctx = Jahia.getThreadParamBean();
        assertNotNull(site);

    }

    public void testImportXML() throws RepositoryException, IOException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        ImportExportService importExportService = ServicesRegistry.getInstance().getImportExportService();
        JCRNodeWrapper stageRootNode = session
                .getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper stageNode = (JCRNodeWrapper) stageRootNode
                .getNode("home");

        InputStream importStream = getClass().getClassLoader()
                .getResourceAsStream("imports/importJCR.xml");
        session.importXML(SITECONTENT_ROOT_NODE, importStream,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
        importStream.close();
        session.save();
    }

    public void testExportDocumentView() throws RepositoryException, IOException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        JCRNodeWrapper stageRootNode = session
                .getNode(SITECONTENT_ROOT_NODE);
        JCRNodeWrapper stageNode = (JCRNodeWrapper) stageRootNode
                .getNode("home");

        FileOutputStream fileOutputStream = new FileOutputStream("home.xml");
        session.exportDocumentView(stageNode.getPath(), fileOutputStream, true, false);
        fileOutputStream.close();
        
    }

    @Override
    protected void tearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
    }
    
}
