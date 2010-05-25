package org.jahia.modules.feedimporter;

import org.apache.commons.vfs.*;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemExistsException;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * NewsML feed importer. This class copies the content of a NewsML feed into a sub-tree in the JCR.
 *
 * @author loom
 *         Date: May 21, 2010
 *         Time: 3:31:51 PM
 */
public class NewsMLImporter {

    private static Logger logger = Logger.getLogger(NewsMLImporter.class);

    public NewsMLImporter() {

    }

    public void processElement(Element element, JCRNodeWrapper node) throws RepositoryException {
        if (element.getAttributeValue("Href") != null) {
            logger.info("Found reference to external resource " + element.getAttributeValue("Href"));
        }
        for (Element childElement : (List<Element>) element.getChildren()) {
            JCRNodeWrapper childNode = node.addNode(childElement.getName(), "jnt:feedContent");
            for (Attribute childElementAttribute : (List<Attribute>) element.getAttributes()) {
                childNode.setProperty(childElementAttribute.getName(), childElementAttribute.getValue());
            }
            processElement(childElement, childNode);
        }
    }

    public void processDocument(Document document, JCRNodeWrapper node) throws RepositoryException {
        Element rootElement = document.getRootElement();
        JCRNodeWrapper newsMLNode = node.addNode(rootElement.getName(), "jnt:feedContent");
        for (Attribute childElementAttribute : (List<Attribute>) rootElement.getAttributes()) {
            newsMLNode.setProperty(childElementAttribute.getName(), childElementAttribute.getValue());
        }
        processElement(rootElement, newsMLNode);
    }

    public void importFeed (String feedURL, String userName, String password, JCRNodeWrapper node, JCRSessionWrapper session) throws IOException, JDOMException, RepositoryException {

        if ((userName != null) && (password != null)) {
            StaticUserAuthenticator auth = new StaticUserAuthenticator(userName, password, null);
            FileSystemOptions opts = new FileSystemOptions();
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
        }

        // Locate the Jar file
        FileSystemManager fsManager = VFS.getManager();
        FileObject jarFile = fsManager.resolveFile( feedURL );

        session.checkout(node);


        // List the children of the Jar file
        FileObject[] children = jarFile.getChildren();
        logger.debug( "Children of " + jarFile.getName().getURI() );
        for ( int i = 0; i < children.length; i++ ) {
            logger.debug( children[ i ].getName().getBaseName() );
            if ("xml".equals(children[i].getName().getExtension().toLowerCase())) {
                InputStream currentNewsItemInputStream = children[i].getContent().getInputStream();
                
                // session.importXML(node.getPath(), currentNewsItemInputStream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
                Document document = new SAXBuilder().build(currentNewsItemInputStream);

                processDocument(document, node);
            }
        }

    }
}
