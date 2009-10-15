/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.decorator;

import org.apache.jackrabbit.value.BinaryImpl;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.IOException;
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

    public void uploadFile(final InputStream is, String contentType) {
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
            try {
                content.setProperty(Constants.JCR_DATA, new BinaryImpl(is));
            } catch (IOException e) {
                logger.error(e);
            }
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
    
    /**
     * The encoding is an optional property, can be null. 
     * @return file encoding or null if not set
     */
    public String getEncoding() {
        try {
            Node content = objectNode.getNode(Constants.JCR_CONTENT);
            if (content.hasProperty(Constants.JCR_ENCODING)) {
                return content.getProperty(Constants.JCR_ENCODING).getString();
            }
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
