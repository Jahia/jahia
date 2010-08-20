package org.jahia.services.content.impl.vfs;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.helper.ContentHubHelper;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.TestHelper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.*;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Aug 20, 2010
 * Time: 3:29:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class VFSContentStoreProviderTest {
    private static Logger logger = Logger.getLogger(VFSContentStoreProviderTest.class);
    private static final String TESTSITE_NAME = "vfsContentProviderTest";
    private static final String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;
    private static JahiaSite site;
    private static File dynamicMountDir;
    private static File staticMountDir;

    @BeforeClass
    public static void oneTimeSetUp()
            throws Exception {
        try {
            site = (JahiaSite) JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        return TestHelper.createSite(TESTSITE_NAME, "localhost", "templates-web", null);
                    }
                    catch (Exception e) {
                        logger.error("Cannot create or publish site", e);

                        session.save();
                    }
                    return null;
                }
            });
            File sysTempDir = new File(System.getProperty("java.io.tmpdir"));

            staticMountDir = new File(sysTempDir, "static-mount");
            if (!staticMountDir.exists()) {
                staticMountDir.mkdir();
            }

            dynamicMountDir = new File(sysTempDir, "dynamic-mount");
            if (!dynamicMountDir.exists())
                dynamicMountDir.mkdir();
        }
        catch (Exception ex) {
            logger.warn("Exception during test setUp", ex);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        try {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (session.nodeExists(SITECONTENT_ROOT_NODE)) {
                TestHelper.deleteSite(TESTSITE_NAME);
            }
            session.save();

            session.logout();
        } catch (Exception ex) {
            logger.warn("Exception during test tearDown", ex);
        }
    }

    @Test
    public void testStaticMount() throws JahiaInitializationException, RepositoryException {
        VFSContentStoreProvider vfsProvider = new VFSContentStoreProvider();
        vfsProvider.setKey("local");
        vfsProvider.setRoot("file://" + staticMountDir.getAbsolutePath());
        vfsProvider.setRmibind("local");
        vfsProvider.setMountPoint("/mounts/static-mount");
        vfsProvider.setUserManagerService(ServicesRegistry.getInstance().getJahiaUserManagerService());
        vfsProvider.setGroupManagerService(ServicesRegistry.getInstance().getJahiaGroupManagerService());
        vfsProvider.setSitesService(ServicesRegistry.getInstance().getJahiaSitesService());
        vfsProvider.setService(ServicesRegistry.getInstance().getJCRStoreService());
        JCRSessionFactory jcrSessionFactory = (JCRSessionFactory) SpringContextSingleton.getInstance().getContext().getBean("jcrSessionFactory");
        vfsProvider.setSessionFactory(jcrSessionFactory);
        JCRPublicationService jcrPublicationService = (JCRPublicationService) SpringContextSingleton.getInstance().getContext().getBean("jcrPublicationService");
        vfsProvider.setPublicationService(jcrPublicationService);
        vfsProvider.start();

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        try {
            JCRNodeWrapper mountNode = session.getNode("/mounts/static-mount");
            assertNode(mountNode, 0);
        } catch (PathNotFoundException pnfe) {
            logger.error("Mount point not available", pnfe);
            Assert.assertTrue("Static mount point is not properly setup", false);
        }

        vfsProvider.stop();
    }

    @Test
    public void testDynamicMount() throws GWTJahiaServiceException, RepositoryException {
        ContentHubHelper contentHubHelper = (ContentHubHelper) SpringContextSingleton.getInstance().getContext().getBean("ContentHubHelper");
        JahiaUser jahiaRootUser = ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdminUser(0);
        contentHubHelper.mount("dynamic-mount", "file://" + dynamicMountDir.getAbsolutePath(), jahiaRootUser);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        try {
            JCRNodeWrapper mountNode = session.getNode("/mounts/dynamic-mount");
            assertNode(mountNode, 0);

            mountNode.remove();
            session.save();
        } catch (PathNotFoundException pnfe) {
            logger.error("Mount point not available", pnfe);
            Assert.assertTrue("Dynamic mount point is not properly setup", false);
        }
    }

    private void assertNode(Node node, int depth)
            throws RepositoryException {
        NodeType primaryNodeType = node.getPrimaryNodeType();
        Assert.assertNotNull("Primary node type is null !", primaryNodeType);
        NodeDefinition nodeDefinition = node.getDefinition();
        Assert.assertNotNull("Node definition is null !", nodeDefinition);
        String nodeIdentifier = node.getIdentifier();
        Assert.assertNotNull("Node identifier is null!", nodeIdentifier);
        NodeType[] nodeMixinNodeTypes = node.getMixinNodeTypes();
        Assert.assertNotNull("Mixin types are null !", nodeMixinNodeTypes);
        Assert.assertNotNull("Node path is null!", node.getPath());
        Assert.assertNotNull("Node name is null!", node.getName());
        int lastSlashPosInPath = node.getPath().lastIndexOf("/");
        if (lastSlashPosInPath > -1)
            Assert.assertEquals("Last part of path and name are not equal !", node.getPath().substring(lastSlashPosInPath + 1), node.getName());
        else {
            Assert.assertEquals("Last part of path and name are not equal !", node.getPath(), node.getName());
        }

        PropertyIterator propertyIterator = node.getProperties();
        Assert.assertNotNull("Property iterator is null !", propertyIterator);
        while (propertyIterator.hasNext()) {
            Property property = propertyIterator.nextProperty();
            property.isMultiple();
            property.isNew();
            property.isNode();
        }
        NodeIterator nodeIterator = node.getNodes();
        Assert.assertNotNull("Child node iterator is null !", nodeIterator);
        while (nodeIterator.hasNext()) {
            Node childNode = nodeIterator.nextNode();
            assertNode(childNode, depth + 1);
        }
    }

}
