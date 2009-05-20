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
 * Time: 15:20:31
 * To change this template use File | Settings | File Templates.
 */
public class DeletedPropertyWrapper {
    private String nodePath;
    private NodeWrapper node;
    private String name;

    public DeletedPropertyWrapper(PropertyWrapper property, KnowledgeHelper drools) throws RepositoryException {
        name = property.getName();
        node = property.getNode();
        nodePath = node.getPath();
        property.getProperty().remove();
        drools.retract(property);
    }

    public DeletedPropertyWrapper(NodeWrapper node, String property) throws RepositoryException {
        this.node = node;
        nodePath = node.getPath();
        name = property;
    }

    public String getName() {
        return name;
    }

    public NodeWrapper getNode() {
        return node;
    }

    public String toString() {
        return "deleted "+nodePath+"/"+name;
    }
}
