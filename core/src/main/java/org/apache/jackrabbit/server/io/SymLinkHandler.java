/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.apache.jackrabbit.server.io;

import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.JcrConstants;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * 
 * User: toto
 * Date: 14 janv. 2008
 * Time: 17:59:02
 * 
 */
public class SymLinkHandler extends DefaultHandler implements IOHandler {

    public SymLinkHandler(IOManager ioManager) {
        super(ioManager);
    }

    @Override
    public boolean canImport(ImportContext importContext, DavResource davResource) {
        return false;
    }

    @Override
    public boolean canImport(ImportContext importContext, boolean b) {
        return false;
    }

    @Override
    public boolean canExport(ExportContext context, boolean isCollection) {
        if (context == null || context.isCompleted()) {
            return false;
        }
        Item exportRoot = context.getExportRoot();
        boolean success = exportRoot != null && exportRoot.isNode();
        if (success && !isCollection) {
            try {
                Node n = ((Node)exportRoot);
                success = n.getPrimaryNodeType().getName().equals("jnt:symLink") && n.hasProperty("j:link");
            } catch (RepositoryException e) {
                // should never occur.
                success = false;
            }
        }
        return success;
    }

    @Override
    protected Node getContentNode(ExportContext context, boolean isCollection) throws RepositoryException {
        Node n = (Node) context.getExportRoot();
        return n.getProperty("j:link").getNode().getNode(JcrConstants.JCR_CONTENT);
    }

}
