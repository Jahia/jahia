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

import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.JcrConstants;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 14 janv. 2008
 * Time: 17:59:02
 * To change this template use File | Settings | File Templates.
 */
public class SymLinkHandler extends DefaultHandler implements IOHandler {

    public SymLinkHandler(IOManager ioManager) {
        super(ioManager);
    }

    public boolean canImport(ImportContext importContext, DavResource davResource) {
        return false;
    }

    public boolean canImport(ImportContext importContext, boolean b) {
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
                success = th.getPrimaryNodeType().getName().equals("jnt:symLink") && th.hasProperty("j:link");
            } catch (RepositoryException e) {
                // should never occur.
                success = false;
            }
        }
        return success;
    }

    protected Node getContentNode(ExportContext context, boolean isCollection) throws RepositoryException {
        Node n = (Node)context.getExportRoot();
        return n.getProperty("j:link").getNode().getNode(JcrConstants.JCR_CONTENT);
    }


}
