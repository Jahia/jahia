package org.jahia.modules.feedimporter;

import org.apache.commons.vfs.*;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            logger.info("Found reference to external resource " + element.getAttributeValue("Href") + " on tag " + element.getName());
        }
        for (Element childElement : (List<Element>) element.getChildren()) {
            JCRNodeWrapper childNode = node.addNode(childElement.getName(), "jnt:feedContent");
            for (Attribute childElementAttribute : (List<Attribute>) element.getAttributes()) {
                childNode.setProperty(childElementAttribute.getName(), childElementAttribute.getValue());
            }
            processElement(childElement, childNode);
        }
    }

    public void processDocument(Document document, JCRNodeWrapper node, String entryBaseName) throws RepositoryException, JDOMException {

        String newsItemID = getElement(document.getRootElement(), "NewsItem/Identification/NewsIdentifier/NewsItemId").getText();
        String newsPublicIdentifier = getElement(document.getRootElement(), "NewsItem/Identification/NewsIdentifier/PublicIdentifier").getText();
        String newsLanguage = getElement(document.getRootElement(), "NewsItem/NewsComponent/DescriptiveMetadata/Language").getAttributeValue("FormalName");
        String newsSubjectCode = getElement(document.getRootElement(), "NewsItem/NewsComponent/DescriptiveMetadata/SubjectCode/Subject").getAttributeValue("FormalName");
        String newsDateStr = getElement(document.getRootElement(), "NewsItem/Identification/NewsIdentifier/DateId").getText();
        String newsUrgency = getElement(document.getRootElement(), "NewsItem/NewsManagement/Urgency").getAttributeValue("FormalName");
        String newsStatus = getElement(document.getRootElement(), "NewsItem/NewsManagement/Status").getAttributeValue("FormalName");
        Element newsInstruction = getElement(document.getRootElement(), "NewsItem/NewsManagement/Instruction");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date newsDate = null;
        try {
            newsDate = dateFormat.parse(newsDateStr);
        } catch (ParseException e) {
            logger.error("Error parsing date " + newsDateStr + ", defaulting to now...", e);
            newsDate = new Date();
        }
        logger.info("Importing news item with date " + newsDate.toString() + " and ID " + newsItemID + " in language " + newsLanguage + " in subject " + newsSubjectCode);

        // todo : we need to check if the language has been configured in the site, otherwise we use the current language.
        JCRSessionWrapper languageSession = JCRSessionFactory.getInstance().getCurrentUserSession(
                node.getSession().getWorkspace().getName(), new Locale(newsLanguage.toLowerCase()));

        JCRNodeWrapper languageNode = languageSession.getNode(node.getPath());
        languageSession.checkout(languageNode);

        JCRNodeWrapper feedEntryNode = languageNode.addNode(entryBaseName, "jnt:feedEntry");

        Element rootElement = document.getRootElement();
        JCRNodeWrapper newsMLNode = feedEntryNode.addNode(rootElement.getName(), "jnt:feedContent");
        for (Attribute childElementAttribute : (List<Attribute>) rootElement.getAttributes()) {
            newsMLNode.setProperty(childElementAttribute.getName(), childElementAttribute.getValue());
        }
        processElement(rootElement, newsMLNode);

        languageSession.save();
    }

    public void importFeed (String feedURL, String userName, String password, JCRNodeWrapper parentNode, JCRSessionWrapper session) throws IOException, JDOMException, RepositoryException {

        if ((userName != null) && (password != null)) {
            StaticUserAuthenticator auth = new StaticUserAuthenticator(userName, password, null);
            FileSystemOptions opts = new FileSystemOptions();
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
        }

        // Locate the Jar file
        FileSystemManager fsManager = VFS.getManager();
        FileObject jarFile = fsManager.resolveFile( feedURL );

        // List the children of the Jar file
        FileObject[] children = jarFile.getChildren();
        logger.debug( "Children of " + jarFile.getName().getURI() );
        for ( int i = 0; i < children.length; i++ ) {
            logger.debug( children[ i ].getName().getBaseName() );
            if ("xml".equals(children[i].getName().getExtension().toLowerCase())) {
                InputStream currentNewsItemInputStream = children[i].getContent().getInputStream();
                
                // session.importXML(node.getPath(), currentNewsItemInputStream, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
                SAXBuilder saxBuilder = new SAXBuilder();
                saxBuilder.setFeature(
                        "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

                Document document = saxBuilder.build(currentNewsItemInputStream);

                processDocument(document, parentNode, children[i].getName().getBaseName());
            }
        }
         session.save();

    }

    /**
     * Utility method to retrieve an XML element using an XPath expression. Note that this method is
     * namespace aware and will require you to use the "xp" prefix in your XPath queries. For example, an XPath query
     * for a Spring XML configuration will look like this :
     * /xp:beans/xp:bean[@id="FileListSync"]/xp:property[@name="syncUrl"]
     * Currently there is no way to rename the prefix.
     * @param scopeElement the scope in which to execute the XPath query
     * @param xPathExpression the XPath query to select the element we wish to retrieve. In the case where multiple
     * elements match, only the first one will be returned.
     * @return the first element that matches the XPath expression, or null if no element matches.
     * @throws JDOMException raised if there was a problem navigating the JDOM structure.
     */
    public Element getElement(Element scopeElement, String xPathExpression) throws JDOMException {
        XPath xPath = XPath.newInstance(xPathExpression);
        String namespaceURI = scopeElement.getDocument().getRootElement().getNamespaceURI();
        if ((namespaceURI != null) && (!"".equals(namespaceURI))) {
            xPath.addNamespace("xp", namespaceURI);
        }
        return (Element) xPath.selectSingleNode(scopeElement);
    }

}
