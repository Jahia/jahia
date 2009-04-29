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
package org.jahia.services.content.automation;

import org.drools.spi.KnowledgeHelper;

import javax.jcr.RepositoryException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 17 janv. 2008
 * Time: 15:17:58
 * To change this template use File | Settings | File Templates.
 */
public class DeletedNodeWrapper {
    private String path;

    public DeletedNodeWrapper(NodeWrapper nodeWrapper, KnowledgeHelper drools) throws RepositoryException {
        path = nodeWrapper.getPath();
        nodeWrapper.getNode().remove();
        drools.retract(nodeWrapper);

        // should also retract properties and subnodes
    }

    public DeletedNodeWrapper(String path) throws RepositoryException {
        this.path = path;
    }

    public String toString() {
        return "deleted "+path;
    }
}
