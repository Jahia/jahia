package org.jahia.services.providers;

import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.junit.Assert.*;

/**
 * Test class for modules provider
 * User: david
 * Date: 11/26/12
 * Time: 10:23 AM
 */
public class ModulesProviderTest {
    private static transient Logger logger = Logger.getLogger(ModulesProviderTest.class);
    
    private final static String mountPoint = "/modulesFileSystem";
    private Node root;
    private JahiaTemplatesPackage artPackage;
    // Read by type
    @Test
    public void readTypes() {
        try {
            Session s = JCRSessionFactory.getInstance().getCurrentUserSession();
            root = s.getNode(mountPoint);

            // get default module
            JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
            artPackage = templateManagerService.getTemplatePackageByFileName("article");

            // Read
            readType("jnt:cssFolder","/css");
            readType("jnt:cssFile","/css/article.css");
            readType("jnt:nodeTypeFolder","/jnt_article");
            readType("jnt:templateTypeFolder","/jnt_article/html");
            readType("jnt:viewFile", "/jnt_article/html/article.jsp");
            readType("jnt:resourceBundleFolder", "/resources");
            readType("jnt:definitionFile", "/META-INF/definitions.cnd");

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    private void readType(String nodeType,String path) throws RepositoryException {
        String resolvedNodeType = root.getNode(artPackage.getRootFolderWithVersion() + path).getPrimaryNodeType().getName();
        assertEquals(nodeType + " is expected on path " + path + " but " + resolvedNodeType + " has been found", resolvedNodeType ,nodeType);

    }

    // Write by type
    @Test
    public void writeAndReadProperties() {
        Session s = null;
        try {
            s = JCRSessionFactory.getInstance().getCurrentUserSession();
            root = s.getNode(mountPoint);

            // get default module
            JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
            artPackage = templateManagerService.getTemplatePackageByFileName("article");

            //read properties
            Node viewNode = root.getNode( artPackage.getRootFolderWithVersion() + "/jnt_article/html/article.jsp");
            assertTrue("article source not match",viewNode.getProperty("sourceCode").getString().startsWith("<%@ taglib prefix=\"jcr\" uri=\"http://www.jahia.org/tags/jcr\" %>"));

            // write properties
            viewNode.setProperty("sourceCode",viewNode.getProperty("sourceCode").getString() + "TEST");
            viewNode.setProperty("cache.perUser",true);
            viewNode.setProperty("cache.requestParameters","carcheTest");
            s.save();

            // read properties
            s.logout();
            artPackage = templateManagerService.getTemplatePackageByFileName("article");
            root = s.getNode(mountPoint);
            viewNode = root.getNode( artPackage.getRootFolderWithVersion() + "/jnt_article/html/article.jsp");
            s = JCRSessionFactory.getInstance().getCurrentUserSession();
            assertTrue("article source not match",viewNode.getProperty("sourceCode").getString().endsWith("TEST"));
            assertTrue("cache.perUser not set to true",viewNode.getProperty("cache.perUser").getBoolean());
            assertTrue("cache.requestParameters not set to carcheTest",viewNode.getProperty("cache.requestParameters").getString().equals("carcheTest"));

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }


}
