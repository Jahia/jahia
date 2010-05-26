package org.jahia.modules.feedimporter;

import org.apache.commons.vfs.*;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.log4j.Logger;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.jcr.ItemExistsException;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    public void processElement(Element element, JCRNodeWrapper node, FileObject contextFileObject, JCRNodeWrapper feedNode) throws RepositoryException, FileSystemException {
        if (element.getAttributeValue("Href") != null) {
            logger.info("Found reference to external resource " + element.getAttributeValue("Href") + " on tag " + element.getName());
            if ("ContentItem".equals(element.getName())) {
                // now let's import the resource file.
                String ref = element.getAttributeValue("Href");
                if (!ref.startsWith("#")) {
                    if (ref.startsWith("http://")) {
                        // todo handle case of external resource.
                    } else {
                        FileObject refFile = contextFileObject.getChild(ref);
                        if (refFile == null) {
                            logger.warn("Couldn't find file " + ref + " in feed location " + contextFileObject.toString());
                        } else {
                            JCRNodeWrapper mediaNode = null;
                            try {
                                mediaNode = feedNode.getNode("medias");
                            } catch (PathNotFoundException pnfe) {
                            }
                            if (mediaNode == null) {
                                mediaNode = feedNode.addNode("medias", "jnt:folder");
                            }
                            logger.info("Storing file " + refFile + " into feed media repository");
                            JCRNodeWrapper existingFileNode = null;
                            try {
                                existingFileNode = mediaNode.getNode(refFile.getName().getBaseName());
                            } catch (PathNotFoundException pnfe) {
                            }
                            if (existingFileNode != null) {
                                // found the node, for the moment we simply keep the old value, but we could create
                                // a new version in a later update.
                            } else {
                                mediaNode.uploadFile(refFile.getName().getBaseName(), refFile.getContent().getInputStream(), JahiaContextLoaderListener.getServletContext().getMimeType(ref));
                            }

                            // todo : we must still create the node reference to be able to render the image in the template.
                        }
                    }
                }
            }
        }
        for (Element childElement : (List<Element>) element.getChildren()) {
            JCRNodeWrapper childNode = getOrCreateChildNode(node, childElement.getName(), "jnt:feedContent");
            for (Attribute childElementAttribute : (List<Attribute>) element.getAttributes()) {
                childNode.setProperty(childElementAttribute.getName(), childElementAttribute.getValue());
            }
            processElement(childElement, childNode, contextFileObject, feedNode);
        }
    }

    public void processNewsComponent(Element newsComponent, JCRNodeWrapper node) {

    }

    public void processDocument(Document document, JCRNodeWrapper node, String entryBaseName, FileObject contextFileObject, JCRNodeWrapper feedNode) throws RepositoryException, JDOMException, FileSystemException {

        String newsItemID = getElement(document.getRootElement(), "NewsItem/Identification/NewsIdentifier/NewsItemId").getText();
        String newsPublicIdentifier = getElement(document.getRootElement(), "NewsItem/Identification/NewsIdentifier/PublicIdentifier").getText();
        String newsLanguage = getElement(document.getRootElement(), "//DescriptiveMetadata/Language").getAttributeValue("FormalName");
        String newsSubjectCode = getElement(document.getRootElement(), "//DescriptiveMetadata/SubjectCode/Subject").getAttributeValue("FormalName");
        String newsDateStr = getElement(document.getRootElement(), "NewsItem/Identification/NewsIdentifier/DateId").getText();
        String newsFirstCreatedDateStr = getElement(document.getRootElement(), "NewsItem/NewsManagement/FirstCreated").getText();
        String newsThisRevisionCreatedDateStr = getElement(document.getRootElement(), "NewsItem/NewsManagement/ThisRevisionCreated").getText();
        String newsUrgency = getElement(document.getRootElement(), "NewsItem/NewsManagement/Urgency").getAttributeValue("FormalName");
        String newsStatus = getElement(document.getRootElement(), "NewsItem/NewsManagement/Status").getAttributeValue("FormalName");
        if ("Embargoed".equals(newsStatus)) {
            Element statusWillChange = getElement(document.getRootElement(), "NewsItem/NewsManagement/StatusWillChange");
            if (statusWillChange != null) {
                Element dateAndTime = getElement(document.getRootElement(), "NewsItem/NewsManagement/StatusWillChange/DateAndTime");
                if (dateAndTime != null) {
                    // date and time is set, let's integrate with time-based publishing.
                    String dateAndTimeStr = dateAndTime.getText();
                }
            }
        }
        Element newsInstruction = getElement(document.getRootElement(), "NewsItem/NewsManagement/Instruction");
        // todo : we need to check if the language has been configured in the site, otherwise we use the current language.
        JCRSessionWrapper languageSession = JCRSessionFactory.getInstance().getCurrentUserSession(
                node.getSession().getWorkspace().getName(), new Locale(newsLanguage.toLowerCase()));

        Query existingNodeQuery = languageSession.getWorkspace().getQueryManager().createQuery("SELECT * FROM [jnt:newsMLItem] as news WHERE news.[itemID]='" + newsItemID + "'", Query.JCR_SQL2);
        QueryResult existingNodeQueryResult = existingNodeQuery.execute();
        NodeIterator existingNodeIterator = existingNodeQueryResult.getNodes();
        JCRNodeWrapper existingNode = null;
        if (existingNodeIterator.hasNext()) {
            existingNode = (JCRNodeWrapper) existingNodeIterator.nextNode();
        }
        boolean mustUpdate = false;
        if (newsInstruction != null) {
            String instructionName = newsInstruction.getAttributeValue("FormalName");
            if ("LiftEmbargo".equals(instructionName)) {
                mustUpdate = true;
            } else if ("Rectify".equals(instructionName)) {
                mustUpdate = true;

            } else if ("Update".equals(instructionName)) {
                mustUpdate = true;

            } else if ("Delete".equals(instructionName)) {
                mustUpdate = true;
            }
        }

        if ((!mustUpdate) && (existingNode != null)) {
            // this shouldn't happen, it means we are trying to import the same node twice !
            logger.error("Error: trying to import as a new node the existing node with ID" + newsItemID + " aborting import !");
            return;
        }
        String newsHeadline = getElement(document.getRootElement(), "NewsItem/NewsComponent/NewsLines/HeadLine").getText();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date newsDate = null;
        try {
            newsDate = dateFormat.parse(newsDateStr);
        } catch (ParseException e) {
            logger.error("Error parsing date " + newsDateStr + ", defaulting to now...", e);
            newsDate = new Date();
        }
        logger.info("Importing news item with date " + newsDate.toString() + " and ID " + newsItemID + " in language " + newsLanguage + " in subject " + newsSubjectCode);

        JCRNodeWrapper languageNode = languageSession.getNode(node.getPath());
        languageSession.checkout(languageNode);

        JCRNodeWrapper feedEntryNode = null;
        if (mustUpdate && (existingNode != null)) {
            languageSession.checkout(existingNode);
            feedEntryNode = existingNode;
            DateTimeFormatter iso8601DateTimeFormatter = ISODateTimeFormat.basicDateTimeNoMillis();
            DateTime thisRevisionCreatedDateTime = iso8601DateTimeFormatter.parseDateTime(newsThisRevisionCreatedDateStr);
            Calendar newsUpdateCalendar = Calendar.getInstance();
            newsUpdateCalendar.setTime(thisRevisionCreatedDateTime.toDate());
            feedEntryNode.setProperty("contentUpdated", newsUpdateCalendar);
        } else {
            feedEntryNode = languageNode.addNode(entryBaseName, "jnt:newsMLItem");
        }
        feedEntryNode.setProperty("jcr:title", newsHeadline);
        Calendar newsCalendar=Calendar.getInstance();
        newsCalendar.setTime(newsDate);
        feedEntryNode.setProperty("date", newsCalendar);
        feedEntryNode.setProperty("itemID", newsItemID);
        feedEntryNode.setProperty("publicIdentifier", newsPublicIdentifier);
        feedEntryNode.setProperty("urgency", Long.parseLong(newsUrgency));
        feedEntryNode.setProperty("status", newsStatus);

        Element rootElement = document.getRootElement();
        JCRNodeWrapper newsMLNode = getOrCreateChildNode(feedEntryNode, rootElement.getName(), "jnt:feedContent");
        for (Attribute childElementAttribute : (List<Attribute>) rootElement.getAttributes()) {
            newsMLNode.setProperty(childElementAttribute.getName(), childElementAttribute.getValue());
        }
        processElement(rootElement, newsMLNode, contextFileObject, feedNode);

        languageSession.save();
    }

    public JCRNodeWrapper getOrCreateChildNode(JCRNodeWrapper parentNode, String childName, String nodeTypeName) throws RepositoryException {
        JCRNodeWrapper result = null;
        try {
            result = parentNode.getNode(childName);
        } catch (PathNotFoundException pnfe) {
            // This is expected. It means we have to create the node.
            result = parentNode.addNode(childName, nodeTypeName);
        }
        return result;
    }

    public class FileObjectComparator implements Comparator<FileObject> {

        public int compare(FileObject o1, FileObject o2) {
            return o1.getName().compareTo(o2.getName());
        }
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

        // List the children of the Jar file and sort them by file name.
        FileObject[] children = jarFile.getChildren();
        Arrays.sort(children, new FileObjectComparator());
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

                if ("NewsML".equals(document.getRootElement().getName())) {
                    logger.info("Importing contents of XML NewsML file " + children[ i ].getName().getBaseName());

                    processDocument(document, parentNode, children[i].getName().getBaseName(), jarFile, parentNode);
                }
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
