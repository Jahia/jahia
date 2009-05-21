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
package org.jahia.services.content.automation;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.drools.spi.KnowledgeHelper;
import org.jahia.api.Constants;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 20 déc. 2007
 * Time: 11:53:45
 * To change this template use File | Settings | File Templates.
 */
public class NodeWrapper implements Updateable {
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(NodeWrapper.class);

    private NodeWrapper parentNode;
    private String parentNodePath;
    private String name;
    private String type;

    private Node node;

    public NodeWrapper(Node node) {
        this.node = node;
    }

    public NodeWrapper(NodeWrapper parentNodeWrapper, String name, String type, KnowledgeHelper drools) throws RepositoryException {
        this.parentNode = parentNodeWrapper;

        Node node = parentNode.getNode();
        parentNodePath = node.getPath();
        this.name = name;

        if (type == null) {
            ExtendedNodeDefinition end = ((ExtendedNodeType)node.getPrimaryNodeType()).getChildNodeDefinitionsAsMap().get(name);
            NodeType nodetype = end.getRequiredPrimaryTypes()[0];
            type = nodetype.getName();
        }

        this.type = type;

        if (node.isLocked()) {
            logger.debug("Node is locked, delay property update to later");
            List<Updateable> list = (List<Updateable>) drools.getWorkingMemory().getGlobal("delayedUpdates");
            list.add(this);
        } else {
            this.node = node.addNode(name, type);
        }
    }

    public void doUpdate(Session s, List<Updateable> delayedUpdates) throws RepositoryException {
        try {
            Node node = (Node) s.getItem(parentNodePath);

            if (node.isLocked()) {
                logger.debug("Node is still locked, delay subnode creation to later");
                delayedUpdates.add(this);
            } else {
                node.addNode(name, type);
            }
        } catch (PathNotFoundException e) {
            logger.warn("Node does not exist "+parentNodePath);
        }
    }

    public String getPath() throws RepositoryException {
        if (node != null) {
            return node.getPath();
        } else if (parentNodePath != null && name != null) {
            return parentNodePath + "/" + name;
        }
        return null;
    }

    public String getName() throws RepositoryException {
        if (node != null) {
            return node.getName();
        } else if (name != null) {
            return name;
        }
        return null;
    }

    public NodeWrapper getContent() throws RepositoryException {
        if (node.hasNode(Constants.JCR_CONTENT)) {
            return new NodeWrapper(node.getNode(Constants.JCR_CONTENT));
        }
        return null;
    }

    public String getMimeType() throws RepositoryException {
        if (node.hasNode(Constants.JCR_CONTENT)) {
            return node.getNode(Constants.JCR_CONTENT).getProperty(Constants.JCR_MIMETYPE).getString();
        }
        return null;
    }

    public List<NodeWrapper> getChildNodes() throws RepositoryException {
        List<NodeWrapper> results = new ArrayList<NodeWrapper>();
        NodeIterator it = node.getNodes();
        while (it.hasNext()) {
            Node n = it.nextNode();
            results.add(new NodeWrapper(n));
        }
        return results;
    }

    public NodeWrapper getParent() throws RepositoryException {
        return new NodeWrapper(node.getParent());
    }

    public List<PropertyWrapper> getProperties() throws RepositoryException {
        List<PropertyWrapper> results = new ArrayList<PropertyWrapper>();
        PropertyIterator it = node.getProperties();
        while (it.hasNext()) {
            Property p = it.nextProperty();
            results.add(new PropertyWrapper(this,p));
        }
        return results;
    }

    public List<String> getTypes() throws RepositoryException {
        List<String> r = new ArrayList<String>();
        recurseOnTypes(r,node.getPrimaryNodeType());
        recurseOnTypes(r,node.getMixinNodeTypes());
        return r;
    }

    private void recurseOnTypes(List<String> res, NodeType... nt) {
        for (NodeType nodeType : nt) {
            if (!res.contains(nodeType.getName())) res.add(nodeType.getName());
            recurseOnTypes(res,nodeType.getSupertypes());
        }
    }

    public void addType(String type, KnowledgeHelper drools) throws RepositoryException {
        node.addMixin(type);
        drools.insert(new PropertyWrapper(this, node.getProperty(Constants.JCR_MIXINTYPES)));
        //        drools.update(this);
    }

    public NodeWrapper getAncestor(String type) throws RepositoryException {
        NodeWrapper ancestor = this;
        try {
            while (!ancestor.getNode().isNodeType(Constants.JAHIANT_PAGE) && (ancestor = ancestor.getParent()) != null) {
                if (ancestor.getNode().isNodeType(type)) {
                    return ancestor;
                }
            }
        } catch (ItemNotFoundException e) {
        }
        return null;
    }

    Node getNode() {
        return node;
    }

    public String toString() {
        try {
            return node.getPath();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }
}
