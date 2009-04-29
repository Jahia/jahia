/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 7 févr. 2008
 * Time: 11:53:30
 * To change this template use File | Settings | File Templates.
 */
public class JCRFileContent {
    protected static final Logger logger = Logger.getLogger(JCRFileContent.class);
    protected JCRNodeWrapper node;
    protected Node objectNode;

    public JCRFileContent(JCRNodeWrapper node, Node objectNode) {
        this.node = node;
        this.objectNode = objectNode;
    }

    public InputStream downloadFile () {
        try {
            Property p = objectNode.getNode(Constants.JCR_CONTENT).getProperty(Constants.JCR_DATA);
            return p.getStream();
        } catch (RepositoryException e) {
            logger.error("Repository error",e);
        }
        return null;
    }

    public void uploadFile(InputStream is, String contentType) {
        try {
            Node content;
            if (objectNode.hasNode(Constants.JCR_CONTENT)) {
                content = objectNode.getNode(Constants.JCR_CONTENT);
            } else {
                content = objectNode.addNode(Constants.JCR_CONTENT, Constants.JAHIANT_RESOURCE);
            }
            if (content.hasProperty(Constants.JCR_DATA)) {
                content.getProperty(Constants.JCR_DATA).remove();
            }
            content.setProperty(Constants.JCR_DATA, is);
            if (contentType == null) {
                contentType = "application/binary";
            }
            content.setProperty(Constants.JCR_MIMETYPE, contentType);
            content.setProperty(Constants.JCR_LASTMODIFIED, Calendar.getInstance());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    public String getContentType() {
        try {
            Node content = objectNode.getNode(Constants.JCR_CONTENT);
            return content.getProperty(Constants.JCR_MIMETYPE).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }

    public long getContentLength() {
        try {
            Node content = objectNode.getNode(Constants.JCR_CONTENT);
            return content.getProperty(Constants.JCR_DATA).getLength();
        } catch (RepositoryException e) {
        }
        return 0L;
    }
    
    public String getExtractedText() {
        try {
            Node content = objectNode.getNode(Constants.JCR_CONTENT);
            Property extractedTextProp = content.getProperty(Constants.EXTRACTED_TEXT);
            return extractedTextProp != null ? extractedTextProp.getString() : null;
        } catch (RepositoryException e) {
        }
        return null;
    }    

    public boolean isImage() {
        try {
            final String extens = node.getPath().substring(node.getPath().lastIndexOf(".")).toLowerCase();
            return (extens.indexOf("jpg") != -1) || (extens.indexOf("jpe") != -1) || (extens.indexOf("gif") != -1) ||
                    (extens.indexOf("png") != -1) || (extens.indexOf("bmp") != -1) || (extens.indexOf("tif") != -1);
        } catch (Exception e) {
            return false;
        }
    }

}
