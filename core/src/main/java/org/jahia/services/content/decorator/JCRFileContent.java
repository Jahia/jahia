/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.decorator;

import org.apache.jackrabbit.value.BinaryImpl;
import org.slf4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MimeTypes;

/**
 * Represents the content node.
 * User: toto
 * Date: 7 févr. 2008
 * Time: 11:53:30
 */
public class JCRFileContent {

    private static final Detector DETECTOR = new DefaultDetector(MimeTypes.getDefaultMimeTypes());
    protected static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JCRFileContent.class);
    protected JCRNodeWrapper node;
    protected Node objectNode;
    protected Node contentNode;

    public JCRFileContent(JCRNodeWrapper node, Node objectNode) {
        this.node = node;
        this.objectNode = objectNode;
    }

    public InputStream downloadFile () {
        try {
            Property p = getContentNode().getProperty(Constants.JCR_DATA);
            return p.getBinary().getStream();
        } catch (RepositoryException e) {
            LOGGER.error("Repository error", e);
        }
        return null;
    }

    public void uploadFile(final InputStream is, String contentType) {
        try {
            Node content;
            if (objectNode.hasNode(Constants.JCR_CONTENT)) {
                content = objectNode.getNode(Constants.JCR_CONTENT);
            } else if (!objectNode.isNodeType(Constants.JAHIANT_RESOURCE)) {
                content = objectNode.addNode(Constants.JCR_CONTENT, Constants.JAHIANT_RESOURCE);
            } else {
                content = objectNode;
            }
            if (content.hasProperty(Constants.JCR_DATA)) {
                content.getProperty(Constants.JCR_DATA).remove();
            }
            Binary bin = null;
            // Content type has to be checked before the instantiation of "BinaryImpl"
            // otherwise, the content type is always "application/octet-stream"
            if (contentType == null && is.markSupported()) {
                long startTime = 0;
                if (LOGGER.isDebugEnabled()) {
                    startTime = System.currentTimeMillis();
                    LOGGER.debug(
                            "We don't have a proper content type for file content {}, let's detecting it...",
                            node.getPath());
                }
                contentType = DETECTOR.detect(is, new Metadata()).toString();
                if (LOGGER.isDebugEnabled()) {
                    startTime = System.currentTimeMillis();
                    LOGGER.debug("Detected content type for file content of node {}: {}. Detection took {} ms",
                            new Object[] { node.getPath(), contentType, System.currentTimeMillis() - startTime });
                }
            }
            if (contentType == null) {
                contentType = "application/binary";
            }
            try {
                bin = new BinaryImpl(is);
                content.setProperty(Constants.JCR_DATA, bin);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                if (bin != null) {
                    bin.dispose();
                }
            }
            content.setProperty(Constants.JCR_MIMETYPE, contentType);
            contentNode = content;
        } catch (RepositoryException | IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

    }

    public String getContentType() {
        try {
            return getContentNode().getProperty(Constants.JCR_MIMETYPE).getString();
        } catch (RepositoryException e) {
        }
        return null;
    }
    
    /**
     * The encoding is an optional property, can be null. 
     * @return file encoding or null if not set
     */
    public String getEncoding() {
        try {
            Node content = getContentNode();
            if (content.hasProperty(Constants.JCR_ENCODING)) {
                return content.getProperty(Constants.JCR_ENCODING).getString();
            }
        } catch (RepositoryException e) {
        }
        return null;
    }

    public long getContentLength() {
        try {
            Node content = getContentNode();
            return content.getProperty(Constants.JCR_DATA).getLength();
        } catch (RepositoryException e) {
        }
        return 0L;
    }
    
    protected Node getContentNode() throws PathNotFoundException, RepositoryException {
        if (contentNode == null) {
            contentNode = objectNode.getNode(Constants.JCR_CONTENT);
        }
        return contentNode;
    }

    public String getExtractedText() {
        try {
            Node content = getContentNode();
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

    public String getText() {
        try {
            Node content = getContentNode();
            Property extractedTextProp = content.getProperty(Constants.JCR_DATA);
            return extractedTextProp != null ? extractedTextProp.getString() : null;
        } catch (RepositoryException e) {
        }
        return null;
    }
}
