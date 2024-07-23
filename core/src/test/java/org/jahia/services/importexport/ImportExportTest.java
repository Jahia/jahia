package org.jahia.services.importexport;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.test.framework.AbstractJUnitTest;
import org.junit.Assert;
import org.junit.Test;

import javax.jcr.ImportUUIDBehavior;
import java.io.InputStream;

public class ImportExportTest  extends AbstractJUnitTest {


    @Test
    public void testImportWithReferenceOnRootNode() throws Exception {
        // Import content
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        InputStream importStream = ImportExportTest.class.getClassLoader().getResourceAsStream("imports/importWithReference.xml");
        session.importXML("/users/root", importStream,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
        importStream.close();
        session.save();
        // Check reference
        JCRNodeWrapper referenceNode = session.getNode("/users/root/parentWithChildReference");
        JCRNodeWrapper childNode = referenceNode.getNode("child2");
        Assert.assertEquals(referenceNode.getProperty("childReference").getNode().getIdentifier(), childNode.getIdentifier());

        JCRNodeWrapper nestedReferenceNode = referenceNode.getNode("list/nestedNodeWithReference");
        JCRNodeWrapper nestedChildNode = nestedReferenceNode.getNode("child3");
        Assert.assertEquals(nestedReferenceNode.getProperty("childReference").getNode().getIdentifier(), nestedChildNode.getIdentifier());

        // remove content
        referenceNode.remove();
        session.save();
    }
}
