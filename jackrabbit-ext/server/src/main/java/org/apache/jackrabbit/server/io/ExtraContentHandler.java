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
package org.apache.jackrabbit.server.io;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.webdav.DavResource;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 12 juil. 2007
 * Time: 20:38:16
 * To change this template use File | Settings | File Templates.
 */
public class ExtraContentHandler extends DefaultHandler implements IOHandler {

    public ExtraContentHandler(IOManager ioManager) {
        super(ioManager);
    }

    public boolean canImport(ImportContext context, boolean isCollection) {
        return false;
    }

    public boolean canImport(ImportContext context, DavResource resource) {
        return false;
    }

    public boolean canExport(ExportContext context, boolean isCollection) {
        if (context == null || context.isCompleted()) {
            return false;
        }
        Item exportRoot = context.getExportRoot();
        boolean success = exportRoot != null && exportRoot.isNode();
        if (success && !isCollection) {
            try {
                Node th = ((Node)exportRoot);
                success = th.isNodeType(JcrConstants.NT_RESOURCE) && th.hasProperty(JcrConstants.JCR_DATA);
            } catch (RepositoryException e) {
                // should never occur.
                success = false;
            }
        }
        return success;
    }

    protected Node getContentNode(ExportContext context, boolean isCollection) throws RepositoryException {
        return (Node)context.getExportRoot();
    }

}
